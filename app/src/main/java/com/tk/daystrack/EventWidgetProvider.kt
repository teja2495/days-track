package com.tk.daystrack

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_event_layout)
        
        // Get the selected event ID from shared preferences
        val sharedPrefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val selectedEventId = sharedPrefs.getString("widget_${appWidgetId}_event_id", null)
        
        if (selectedEventId != null) {
            val eventRepository = EventRepository(context)
            val events = eventRepository.loadEvents()
            val selectedEvent = events.find { it.id == selectedEventId }
            
            if (selectedEvent != null) {
                // Update widget with event information
                views.setTextViewText(R.id.widget_event_name, selectedEvent.name)
                
                // Get the most recent instance
                val mostRecentInstance = selectedEvent.instances.maxByOrNull { it.date }
                if (mostRecentInstance != null) {
                    // Get the current display mode (days or months/years)
                    val showDaysOnly = sharedPrefs.getBoolean("widget_${appWidgetId}_show_days_only", true)
                    
                    // Show appropriate text based on display mode
                    val dateText = if (showDaysOnly) {
                        formatDaysText(mostRecentInstance.date)
                    } else {
                        formatWidgetTimeDifference(mostRecentInstance.date)
                    }
                    views.setTextViewText(R.id.widget_event_date, android.text.Html.fromHtml(dateText))
                    
                    // Show note if available
                    if (!mostRecentInstance.note.isNullOrBlank()) {
                        views.setTextViewText(R.id.widget_event_note, mostRecentInstance.note)
                        views.setViewVisibility(R.id.widget_event_note, android.view.View.VISIBLE)
                    } else {
                        views.setViewVisibility(R.id.widget_event_note, android.view.View.GONE)
                    }
                } else {
                    views.setTextViewText(R.id.widget_event_date, context.getString(R.string.widget_no_instances))
                    views.setViewVisibility(R.id.widget_event_note, android.view.View.GONE)
                }
                
                // Set click intent for event title to open event details
                val eventDetailsIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("selected_event_id", selectedEventId)
                }
                val eventDetailsPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId * 2, // Use different request code to avoid conflicts
                    eventDetailsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_event_name, eventDetailsPendingIntent)
                
                // Set click intent for date area to toggle display mode
                val toggleIntent = Intent(context, EventWidgetProvider::class.java).apply {
                    action = "TOGGLE_DISPLAY_MODE"
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                val togglePendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    toggleIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_event_date, togglePendingIntent)
                
                // Set click intent for + button to open add instance dialog
                val addInstanceIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("selected_event_id", selectedEventId)
                    putExtra("open_add_instance", true)
                }
                // Add button removed from widget
                
            } else {
                // Event not found, show placeholder
                views.setTextViewText(R.id.widget_event_name, context.getString(R.string.widget_event_not_found))
                views.setTextViewText(R.id.widget_event_date, context.getString(R.string.widget_tap_to_configure))
                views.setViewVisibility(R.id.widget_event_note, android.view.View.GONE)
                
                // Set click intent to open widget configuration
                val intent = Intent(context, WidgetConfigActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_event_name, pendingIntent)
                views.setOnClickPendingIntent(R.id.widget_event_date, pendingIntent)
                
                // Add button removed from widget
            }
        } else {
            // No event selected, show placeholder
            views.setTextViewText(R.id.widget_event_name, context.getString(R.string.widget_no_event_selected))
            views.setTextViewText(R.id.widget_event_date, context.getString(R.string.widget_tap_to_configure))
            views.setViewVisibility(R.id.widget_event_note, android.view.View.GONE)
            
            // Set click intent to open widget configuration
            val intent = Intent(context, WidgetConfigActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_event_name, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_event_date, pendingIntent)
            
            // Add button removed from widget
        }
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == "TOGGLE_DISPLAY_MODE") {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // Toggle the display mode
                val sharedPrefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                val currentShowDaysOnly = sharedPrefs.getBoolean("widget_${appWidgetId}_show_days_only", true)
                sharedPrefs.edit().putBoolean("widget_${appWidgetId}_show_days_only", !currentShowDaysOnly).apply()
                
                // Update the widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun formatDaysText(eventDate: LocalDate): String {
        val today = LocalDate.now()
        val daysDifference = java.time.temporal.ChronoUnit.DAYS.between(today, eventDate)
        
        return when {
            daysDifference == 0L -> "today"
            daysDifference > 0 -> "in <big><b>${daysDifference}</b></big><br/>days"
            else -> "<big><b>${kotlin.math.abs(daysDifference)}</b></big><br/>days ago"
        }
    }

    private fun formatWidgetTimeDifference(eventDate: LocalDate): String {
        val today = LocalDate.now()
        val period = java.time.Period.between(today, eventDate)
        val years = period.years
        val months = period.months
        val days = period.days
        
        return when {
            // Future dates
            years > 0 || (years == 0 && months >= 12) -> {
                when {
                    years == 0 -> "in <big><b>$months</b></big> months"
                    years == 1 && months == 0 -> "in <big><b>1</b></big> year"
                    years == 1 -> "in <big><b>1</b></big> year <big><b>$months</b></big> months"
                    months == 0 -> "in <big><b>$years</b></big> years"
                    else -> "in <big><b>$years</b></big> years <big><b>$months</b></big> months"
                }
            }
            months > 0 -> {
                when {
                    months == 1 && days == 0 -> "in <big><b>1</b></big> month"
                    months == 1 -> "in <big><b>1</b></big> month <big><b>$days</b></big> days"
                    days == 0 -> "in <big><b>$months</b></big> months"
                    else -> "in <big><b>$months</b></big> months <big><b>$days</b></big> days"
                }
            }
            days > 0 -> {
                when {
                    days == 1 -> "in <big><b>1</b></big> day"
                    else -> "in <big><b>$days</b></big> days"
                }
            }
            days == 0 -> "today"
            // Past dates
            else -> {
                val pastPeriod = java.time.Period.between(eventDate, today)
                val pastYears = pastPeriod.years
                val pastMonths = pastPeriod.months
                val pastDays = pastPeriod.days
                
                when {
                    pastYears > 0 || (pastYears == 0 && pastMonths >= 12) -> {
                        when {
                            pastYears == 0 -> "<big><b>$pastMonths</b></big> months ago"
                            pastYears == 1 && pastMonths == 0 -> "<big><b>1</b></big> year ago"
                            pastYears == 1 -> "<big><b>1</b></big> year <big><b>$pastMonths</b></big> months ago"
                            pastMonths == 0 -> "<big><b>$pastYears</b></big> years ago"
                            else -> "<big><b>$pastYears</b></big> years <big><b>$pastMonths</b></big> months ago"
                        }
                    }
                    pastMonths > 0 -> {
                        when {
                            pastMonths == 1 && pastDays == 0 -> "<big><b>1</b></big> month ago"
                            pastMonths == 1 -> "<big><b>1</b></big> month <big><b>$pastDays</b></big> days ago"
                            pastDays == 0 -> "<big><b>$pastMonths</b></big> months ago"
                            else -> "<big><b>$pastMonths</b></big> months <big><b>$pastDays</b></big> days ago"
                        }
                    }
                    pastDays > 0 -> {
                        when {
                            pastDays == 1 -> "<big><b>1</b></big> day ago"
                            else -> "<big><b>$pastDays</b></big> days ago"
                        }
                    }
                    else -> "today"
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // Clean up shared preferences when widget is deleted
        val sharedPrefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        for (appWidgetId in appWidgetIds) {
            sharedPrefs.edit().remove("widget_${appWidgetId}_event_id").apply()
            sharedPrefs.edit().remove("widget_${appWidgetId}_show_days_only").apply()
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, EventWidgetProvider::class.java)
            )
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, EventWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }
} 