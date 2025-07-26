package com.tk.daystrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tk.daystrack.ui.theme.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import com.tk.daystrack.components.*
import com.tk.daystrack.DateUtils
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DayTrackTheme {
                val repository = EventRepository(this)
                val viewModel: EventViewModel = viewModel { EventViewModel(repository) }

                // State for triggering export/import
                var exportTrigger by remember { mutableStateOf(false) }
                var importTrigger by remember { mutableStateOf(false) }

                // Export launcher
                val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
                    uri?.let {
                        val json = viewModel.exportEventsJson()
                        contentResolver.openOutputStream(it)?.use { output ->
                            output.write(json.toByteArray())
                        }
                    }
                }

                // Import launcher
                val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                    uri?.let {
                        contentResolver.openInputStream(it)?.use { input ->
                            val json = input.bufferedReader().readText()
                            viewModel.importEventsJson(json)
                        }
                    }
                }

                // State for showing import confirmation dialog
                var showImportConfirmDialog by remember { mutableStateOf(false) }

                DayTrackAppWithExportImport(
                    viewModel = viewModel,
                    onExport = {
                        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                        val now = Date()
                        val fileName = "backup-" + sdf.format(now) + ".daystrack"
                        exportLauncher.launch(fileName)
                    },
                    onImport = { showImportConfirmDialog = true }
                )

                // Confirmation dialog for import
                if (showImportConfirmDialog) {
                    ConfirmationDialog(
                        onDismiss = { showImportConfirmDialog = false },
                        onConfirm = {
                            showImportConfirmDialog = false
                            importLauncher.launch("application/octet-stream")
                        },
                        title = "Warning",
                        message = "Importing will overwrite all your existing events data. Are you sure you want to continue?",
                        confirmText = "Yes, Import"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayTrackAppWithExportImport(
    viewModel: EventViewModel,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val currentSortOption by viewModel.currentSortOption.collectAsState()
    val currentFontSize by viewModel.currentFontSize.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
    val eventToUpdate by viewModel.eventToUpdate.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val showToggleDateHint by viewModel.showToggleDateHint.collectAsState()
    val shouldShowToggleHint = showToggleDateHint && events.any { event ->
        event.instances.isNotEmpty() && DateUtils.isAtLeastOneMonth(event.instances.last().date)
    }

    var showSettings by remember { mutableStateOf(false) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    var eventForNewInstance by remember { mutableStateOf<Event?>(null) }
    var eventPendingDelete by remember { mutableStateOf<Event?>(null) }

    val selectedEventForDetails = selectedEventId?.let { id ->
        events.find { it.id == id }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray900)
    ) {
        when {
            showSettings -> {
                BackHandler(enabled = true) {
                    showSettings = false
                }
                SettingsScreen(
                    onBackPressed = { showSettings = false },
                    currentSortOption = currentSortOption,
                    onSortOptionSelected = { viewModel.setSortOption(it) },
                    currentFontSize = currentFontSize,
                    onFontSizeSelected = { viewModel.setFontSize(it) },
                    onExportClick = onExport,
                    onImportClick = onImport
                )
            }
            selectedEventForDetails != null -> {
                BackHandler(enabled = true) {
                    selectedEventId = null
                }
                EventDetailsScreen(
                    event = selectedEventForDetails!!,
                    onBack = { selectedEventId = null },
                    onDelete = {
                        viewModel.removeEvent(selectedEventForDetails!!.id)
                        selectedEventId = null
                    },
                    onDeleteDate = { dateToDelete ->
                        viewModel.deleteEventDate(selectedEventForDetails!!.id, dateToDelete)
                    },
                    onUpdateNote = { date, note ->
                        viewModel.updateEventInstanceNote(selectedEventForDetails!!.id, date, note)
                    },
                    viewModel = viewModel,
                    fontSize = currentFontSize
                )
            }
            else -> {
                if (isEditMode) {
                    BackHandler(enabled = true) {
                        viewModel.setEditMode(false)
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                ) {
                    AppHeader(
                        title = "Days Track",
                        onSettingsClick = {
                            if (isEditMode) viewModel.setEditMode(false)
                            showSettings = true
                        }
                    )
                    
                    if (viewModel.showEventListHintBanner.collectAsState().value) {
                        HintBanner(
                            message = "Hint: Long press an event to enter edit mode, where you can delete and reorder events.",
                            onDismiss = { viewModel.dismissEventListHintBanner() }
                        )
                    }
                    
                    if (shouldShowToggleHint) {
                        HintBanner(
                            message = "Hint: Tap the date text to switch between days and months/years.",
                            onDismiss = { viewModel.dismissToggleDateHint() }
                        )
                    }
                    
                    if (events.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateMessage(
                                title = "No events yet",
                                message = "Tap the Add Event button to add your first event and start tracking important dates"
                            )
                        }
                    } else {
                        val reorderableState = rememberReorderableLazyListState(
                            onMove = { from, to ->
                                viewModel.reorderEvents(from.index, to.index)
                            }
                        )
                        
                        EventList(
                            events = events,
                            isEditMode = isEditMode,
                            reorderableState = reorderableState,
                            onEventClick = { selectedEventId = it },
                            onEventLongPress = { viewModel.setEditMode(true) },
                            onEventUpdate = { eventToUpdate -> eventForNewInstance = eventToUpdate },
                            onEventDelete = { viewModel.removeEvent(it) },
                            fontSize = currentFontSize
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp, end = 20.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (isEditMode) {
                        DoneEditingFAB(onClick = { viewModel.setEditMode(false) })
                    } else {
                        AddEventFAB(onClick = { viewModel.showAddDialog() })
                    }
                }
                
                if (showAddDialog) {
                    AddEventBottomSheet(
                        onDismiss = { viewModel.hideAddDialog() },
                        onSave = { name, _, _ ->
                            viewModel.addEvent(name)
                        },
                        showDateField = false,
                        allInstanceDates = emptyList()
                    )
                }
                
                if (eventForNewInstance != null) {
                    AddEventBottomSheet(
                        onDismiss = { eventForNewInstance = null },
                        onSave = { name, date, note ->
                            if (date != null) {
                                viewModel.addInstanceToEvent(
                                    eventForNewInstance!!.id,
                                    name,
                                    EventInstance(date, note)
                                )
                            }
                            eventForNewInstance = null
                        },
                        initialName = eventForNewInstance!!.name,
                        initialDate = java.time.LocalDate.now(),
                        title = eventForNewInstance!!.name,
                        buttonLabel = "Save",
                        editableName = false,
                        showDateField = true,
                        dateFieldLabel = "New Instance",
                        allInstanceDates = eventForNewInstance!!.instances.map { it.date }
                    )
                }
                
                if (showUpdateDialog && eventToUpdate != null) {
                    UpdateEventDialog(
                        event = eventToUpdate!!,
                        onDismiss = { viewModel.hideUpdateDialog() },
                        onUpdate = { newName, newDate, note ->
                            viewModel.updateEventInstance(newName, newDate, note)
                        },
                        onDelete = { eventId ->
                            viewModel.removeEvent(eventId)
                        }
                    )
                }
                
                if (eventPendingDelete != null) {
                    ConfirmationDialog(
                        onDismiss = { eventPendingDelete = null },
                        onConfirm = {
                            viewModel.removeEvent(eventPendingDelete!!.id)
                            eventPendingDelete = null
                        },
                        title = "Delete Event",
                        message = "Are you sure you want to delete this event?",
                        confirmText = "Delete"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortDropdown(
    currentSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val sortOptionText = when (currentSortOption) {
        SortOption.DATE_ASCENDING -> "Date (Ascending)"
        SortOption.DATE_DESCENDING -> "Date (Descending)"
        SortOption.ALPHABETICAL -> "Alphabetical"
        SortOption.CUSTOM -> "Custom (Manual Order)"
    }
    
    Surface(
        onClick = { expanded = true },
        shape = RoundedCornerShape(24.dp),
        color = Gray800,
        contentColor = White,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Sort by: $sortOptionText",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Sort options"
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Date (Ascending)") },
                onClick = {
                    onSortOptionSelected(SortOption.DATE_ASCENDING)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Date (Descending)") },
                onClick = {
                    onSortOptionSelected(SortOption.DATE_DESCENDING)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Alphabetical") },
                onClick = {
                    onSortOptionSelected(SortOption.ALPHABETICAL)
                    expanded = false
                }
            )
        }
    }
}