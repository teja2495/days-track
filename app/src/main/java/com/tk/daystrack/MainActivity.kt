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

    var showSettings by remember { mutableStateOf(false) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    var eventForNewInstance by remember { mutableStateOf<Event?>(null) }

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
                    }
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
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
                            onClick = { showSettings = true },
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
                    if (events.isEmpty()) {
                        EmptyEventsMessage(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(events.size) { index ->
                                val event = events[index]
                                val isLastItem = index == events.size - 1
                                EventListItem(
                                    event = event,
                                    onUpdate = { eventToUpdate ->
                                        eventForNewInstance = eventToUpdate
                                    },
                                    onClick = { selectedEventId = event.id },
                                    modifier = if (isLastItem) {
                                        Modifier.padding(bottom = 100.dp)
                                    } else {
                                        Modifier
                                    }
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
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
                if (showAddDialog) {
                    AddEventBottomSheet(
                        onDismiss = { viewModel.hideAddDialog() },
                        onSave = { name, date ->
                            viewModel.addEvent(name, date)
                        }
                    )
                }
                if (eventForNewInstance != null) {
                    AddEventBottomSheet(
                        onDismiss = { eventForNewInstance = null },
                        onSave = { name, date ->
                            viewModel.addInstanceToEvent(eventForNewInstance!!.id, name, date)
                            eventForNewInstance = null
                        },
                        initialName = eventForNewInstance!!.name,
                        initialDate = java.time.LocalDate.now(),
                        title = eventForNewInstance!!.name,
                        buttonLabel = "Save",
                        editableName = false,
                        dateFieldLabel = "New Instance"
                    )
                }
                if (showUpdateDialog && eventToUpdate != null) {
                    UpdateEventDialog(
                        event = eventToUpdate!!,
                        onDismiss = { viewModel.hideUpdateDialog() },
                        onUpdate = { newName, newDate ->
                            viewModel.updateEventDate(newName, newDate)
                        },
                        onDelete = { eventId ->
                            viewModel.removeEvent(eventId)
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