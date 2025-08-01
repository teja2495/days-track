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
                        DateUtils.formatTimeDifferenceCached(mostRecentInstance.date)
                    }
                    views.setTextViewText(R.id.widget_event_date, dateText)
                    
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
                
                // Set click intent to toggle display mode
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
                views.setOnClickPendingIntent(R.id.widget_container, togglePendingIntent)
                
                // Set click intent for + button to open add instance dialog
                val addInstanceIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("selected_event_id", selectedEventId)
                    putExtra("open_add_instance", true)
                }
                val addInstancePendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId * 2, // Different request code to avoid conflicts
                    addInstanceIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_add_button, addInstancePendingIntent)
                
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
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                
                // Hide add button when no event is configured
                views.setViewVisibility(R.id.widget_add_button, android.view.View.GONE)
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
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // Hide add button when no event is configured
            views.setViewVisibility(R.id.widget_add_button, android.view.View.GONE)
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
            daysDifference > 0 -> "in ${daysDifference} days"
            else -> "${kotlin.math.abs(daysDifference)} days ago"
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