package com.tk.daystrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tk.daystrack.ui.theme.*
import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import com.tk.daystrack.DateUtils

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

                DayTrackAppWithExportImport(
                    viewModel = viewModel,
                    onExport = {
                        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                        val now = Date()
                        val fileName = "backup-" + sdf.format(now) + ".daystrack"
                        exportLauncher.launch(fileName)
                    },
                    onImport = { importLauncher.launch("application/octet-stream") }
                )
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
    var eventPendingDelete by remember { mutableStateOf<Event?>(null) } // <-- Add this

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
                    viewModel = viewModel // <-- Pass the viewModel here
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 22.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Days Track",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                if (isEditMode) viewModel.setEditMode(false)
                                showSettings = true
                            },
                            modifier = Modifier.padding(end = 18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    if (viewModel.showEventListHintBanner.collectAsState().value) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hint: Long press an event to enter edit mode, where you can delete and reorder events.",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Medium
                                )
                                IconButton(onClick = { viewModel.dismissEventListHintBanner() }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Dismiss",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                    if (shouldShowToggleHint) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hint: Tap the date text to switch between days and months/years.",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Medium
                                )
                                IconButton(onClick = { viewModel.dismissToggleDateHint() }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Dismiss",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                    if (events.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyEventsMessage()
                        }
                    } else {
                        val reorderableState = rememberReorderableLazyListState(
                            onMove = { from, to ->
                                viewModel.reorderEvents(from.index, to.index)
                            }
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent
                        ) {
                            LazyColumn(
                                modifier = if (isEditMode) {
                                    Modifier.fillMaxWidth().reorderable(reorderableState)
                                } else {
                                    Modifier.fillMaxWidth()
                                },
                                state = reorderableState.listState,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(events.size, key = { events[it].id }) { index ->
                                    val event = events[index]
                                    val isLastItem = index == events.size - 1
                                    val itemModifier = if (isLastItem) Modifier.padding(bottom = 120.dp) else Modifier
                                    if (isEditMode) {
                                        ReorderableItem(reorderableState, key = event.id) { isDragging ->
                                            EventListItem(
                                                event = event,
                                                onUpdate = { updatedEvent ->
                                                    if (updatedEvent.name != event.name) {
                                                        viewModel.updateEventName(event.id, updatedEvent.name)
                                                    } else {
                                                        eventPendingDelete = updatedEvent
                                                    }
                                                },
                                                onClick = null,
                                                onLongPress = null,
                                                modifier = itemModifier,
                                                editMode = true,
                                                reorderableState = reorderableState
                                            )
                                        }
                                    } else {
                                        EventListItem(
                                            event = event,
                                            onUpdate = { eventToUpdate -> eventForNewInstance = eventToUpdate },
                                            onClick = { selectedEventId = event.id },
                                            onLongPress = { viewModel.setEditMode(true) },
                                            modifier = itemModifier,
                                            editMode = false,
                                            reorderableState = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 70.dp, end = 20.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (isEditMode) {
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.setEditMode(false) },
                            shape = RoundedCornerShape(50),
                            containerColor = Teal400,
                            contentColor = Color.Black,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done Editing",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            text = {
                                Text(
                                    "Done Editing",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    } else {
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.showAddDialog() },
                            shape = RoundedCornerShape(50),
                            containerColor = Teal500,
                            contentColor = Gray900,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            text = {
                                Text(
                                    "Add Event",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
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
                    AlertDialog(
                        onDismissRequest = { eventPendingDelete = null },
                        containerColor = Gray800,
                        title = { Text("Delete Event") },
                        text = { Text("Are you sure you want to delete this event?") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.removeEvent(eventPendingDelete!!.id)
                                eventPendingDelete = null
                            }) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { eventPendingDelete = null }) {
                                Text("Cancel")
                            }
                        }
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

@Composable
fun EmptyEventsMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "No events yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tap the Add Event button to add your first event and start tracking important dates",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}