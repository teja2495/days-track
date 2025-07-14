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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tk.daystrack.ui.theme.DayTrackTheme
import com.tk.daystrack.EventDetailsScreen
import com.tk.daystrack.ui.theme.DayTrackBackgroundBrush
import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DayTrackTheme {
                val repository = EventRepository(this)
                val viewModel: EventViewModel = viewModel { EventViewModel(repository) }
                DayTrackApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayTrackApp(viewModel: EventViewModel) {
    val events by viewModel.events.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val currentSortOption by viewModel.currentSortOption.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
    val eventToUpdate by viewModel.eventToUpdate.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    
    // Derive selectedEventForDetails from events list to ensure it's always up to date
    val selectedEventForDetails = selectedEventId?.let { id ->
        events.find { it.id == id }
    }

    // Playful gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DayTrackBackgroundBrush())
    ) {
        when {
            showSettings -> {
                // Handle Android back gesture to close settings
                BackHandler(enabled = true) {
                    showSettings = false
                }
                SettingsScreen(
                    onBackPressed = { showSettings = false },
                    currentSortOption = currentSortOption,
                    onSortOptionSelected = { viewModel.setSortOption(it) }
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
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent, // Let the gradient shine through
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Days Track",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            },
                            actions = {
                                Spacer(modifier = Modifier.width(8.dp)) // Add space before the icon
                                IconButton(
                                    onClick = { showSettings = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp)) // Add space after the icon
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.showAddDialog() },
                            shape = RoundedCornerShape(50),
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )
                            },
                            text = {
                                Text("Add Event")
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (events.isEmpty()) {
                            EmptyEventsMessage(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(events) { event ->
                                        EventListItem(
                                            event = event,
                                            onUpdate = { eventToUpdate ->
                                                viewModel.showUpdateDialog(eventToUpdate)
                                            },
                                            onClick = { selectedEventId = event.id }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Dialogs outside Scaffold to avoid dimming issues
                if (showAddDialog) {
                    AddEventDialog(
                        onDismiss = { viewModel.hideAddDialog() },
                        onSave = { name, date ->
                            viewModel.addEvent(name, date)
                        }
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
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tap the Add Event button to add your first event and start tracking important dates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}