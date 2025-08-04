package com.tk.daystrack

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import com.tk.daystrack.components.EventList
import com.tk.daystrack.components.AppHeader
import com.tk.daystrack.components.EmptyStateMessage
import com.tk.daystrack.FontSize
import com.tk.daystrack.ui.theme.Gray900
import com.tk.daystrack.ui.theme.Dimensions

class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)
        
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        // If they gave us an invalid widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Gray900
                ) {
                    WidgetConfigScreen(
                        appWidgetId = appWidgetId,
                        onEventSelected = { eventId ->
                            saveWidgetConfiguration(eventId)
                            finishWithSuccess()
                        }
                    )
                }
            }
        }
    }
    
    private fun saveWidgetConfiguration(eventId: String) {
        val sharedPrefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("widget_${appWidgetId}_event_id", eventId)
            .putBoolean("widget_${appWidgetId}_show_days_only", true) // Set default to show days
            .apply()
    }
    
    private fun finishWithSuccess() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        EventWidgetProvider().onUpdate(this, appWidgetManager, intArrayOf(appWidgetId))
        
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }
}

@Composable
fun WidgetConfigScreen(
    appWidgetId: Int,
    onEventSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val eventRepository = remember { EventRepository(context) }
    val events by remember { mutableStateOf(eventRepository.loadEvents()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimensions.paddingMedium, vertical = Dimensions.paddingExtraLarge)
    ) {
        AppHeader(
            title = context.getString(R.string.main_title),
            onSettingsClick = { /* No settings needed for widget config */ },
            showSettings = false
        )
        
        if (events.isNotEmpty()) {
            Text(
                text = context.getString(R.string.widget_config_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(start = Dimensions.paddingSmall, top = 16.dp, bottom = 16.dp)
            )
        }
        
        if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateMessage(
                    title = context.getString(R.string.main_empty_state_title),
                    message = context.getString(R.string.widget_config_no_events)
                )
            }
        } else {
            EventList(
                events = events,
                isEditMode = false,
                reorderableState = null,
                onEventClick = { eventId -> onEventSelected(eventId) },
                onEventLongPress = { /* No long press action needed for widget config */ },
                onEventUpdate = { /* No update action needed for widget config */ },
                onEventDelete = { /* No delete action needed for widget config */ },
                onUpdateEventName = null,
                fontSize = FontSize.MEDIUM,
                modifier = Modifier.fillMaxSize(),
                showAddButton = false,
                onQuickAdd = null
            )
        }
    }
} 