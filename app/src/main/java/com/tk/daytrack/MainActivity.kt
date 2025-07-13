package com.tk.daytrack

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tk.daytrack.ui.theme.DayTrackTheme

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
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Day Track",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SortDropdown(
                            currentSortOption = currentSortOption,
                            onSortOptionSelected = { viewModel.setSortOption(it) }
                        )
                    }
                    
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
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
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