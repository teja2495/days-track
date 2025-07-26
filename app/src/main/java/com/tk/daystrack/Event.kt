package com.tk.daystrack

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tk.daystrack.DateUtils.toTitleCase
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import java.util.UUID
import java.time.LocalDate
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.tk.daystrack.ui.theme.*
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import com.tk.daystrack.TimelineConnector
import androidx.compose.foundation.clickable
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.platform.LocalContext
import com.tk.daystrack.EventRepository
import androidx.compose.material.icons.filled.Add
import com.tk.daystrack.EventViewModel

data class EventInstance(
    val date: LocalDate,
    val note: String? = null
)

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val instances: List<EventInstance>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    event: Event, 
    onBack: () -> Unit, 
    onDelete: (() -> Unit)? = null,
    onDeleteDate: ((LocalDate) -> Unit)? = null,
    onUpdateNote: ((LocalDate, String) -> Unit)? = null,
    viewModel: EventViewModel? = null // <-- Add this parameter
) {
    val showDialog = remember { mutableStateOf(false) }
    val showDeleteDateDialog = remember { mutableStateOf<LocalDate?>(null) }
    // State for bottom sheet
    val showEditNoteSheet = remember { mutableStateOf(false) }
    val editingNoteText = remember { mutableStateOf("") }
    val editingNoteDate = remember { mutableStateOf<LocalDate?>(null) }
    val initialNoteText = remember { mutableStateOf("") }
    // Add state for delete note dialog
    val showDeleteNoteDialog = remember { mutableStateOf<LocalDate?>(null) }
    val context = LocalContext.current
    val repository = remember { EventRepository(context) }
    val showBanner = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showBanner.value = !repository.getHasSeenNoteHintBanner()
    }
    // Move showAddInstanceSheet here so it's accessible in AppBar actions
    val showAddInstanceSheet = remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = event.name.toTitleCase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    // Only show + icon if there are instances
                    if (event.instances.isNotEmpty()) {
                        IconButton(onClick = { showAddInstanceSheet.value = true }, modifier = Modifier.padding(end = 28.dp)) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Instance",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    // Removed delete icon from app bar
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        // --- EMPTY STATE HANDLING ---
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(0f)
            )
            if (event.instances.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No event dates recorded yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Button(
                        onClick = { showAddInstanceSheet.value = true },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonContainerColor),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Instance",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Instance", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .zIndex(1f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        if (showBanner.value) {
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
                                        text = "Hint: Tap on the date card to add a note for that instance.",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.weight(1f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    IconButton(onClick = {
                                        showBanner.value = false
                                        repository.setHasSeenNoteHintBanner(true)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Dismiss",
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                        // Average frequency always visible
                        val sortedInstances = event.instances.sortedByDescending { it.date }
                        val avgFrequency = DateUtils.averageFrequency(sortedInstances.map { it.date })
                        if (avgFrequency != null) {
                            Column {
                                // Then average frequency
                                Row(
                                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Average frequency: ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "${"%.1f".format(avgFrequency)} days",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = White
                                    )
                                }
                            }
                        }
                        // Scrollable list with extra bottom padding and corner radius
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent
                        ) {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp)
                            ) {
                                if (avgFrequency == null) {
                                    // To keep indices correct if avgFrequency is not shown
                                }
                                items(sortedInstances.size) { index ->
                                    val instance = sortedInstances[index]
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val note = instance.note ?: ""
                                                editingNoteText.value = note
                                                initialNoteText.value = note
                                                editingNoteDate.value = instance.date
                                                showEditNoteSheet.value = true
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = instance.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (onDeleteDate != null) {
                                                    IconButton(
                                                        onClick = { showDeleteDateDialog.value = instance.date }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Delete Date",
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // Show note in a separate card extending from the date card
                                    if (!instance.note.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 12.dp, end = 12.dp)
                                                .clickable {
                                                    val note = instance.note ?: ""
                                                    editingNoteText.value = note
                                                    initialNoteText.value = note
                                                    editingNoteDate.value = instance.date
                                                    showEditNoteSheet.value = true
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = instance.note,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 10.dp)
                                                )
                                                IconButton(
                                                    onClick = { showDeleteNoteDialog.value = instance.date }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Delete Note",
                                                        tint = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    // Centered pipe symbol and interval text
                                    if (index < sortedInstances.size - 1) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                TimelineConnector()
                                                val currentDate = instance.date
                                                val nextDate = sortedInstances[index + 1].date
                                                val daysBetween = nextDate.toEpochDay() - currentDate.toEpochDay()
                                                val intervalText = when {
                                                    daysBetween == -1L -> "1 day earlier"
                                                    daysBetween < -1L -> "${-daysBetween} days earlier"
                                                    daysBetween == 0L -> "Same day"
                                                    else -> "$daysBetween days later"
                                                }
                                                Text(
                                                    text = intervalText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = ThemeTextColor,
                                                    modifier = Modifier
                                                        .padding(top = 0.dp),
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                                // Add total instances text at the end of the list
                                if (sortedInstances.size > 5) {
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp, bottom = 8.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "${sortedInstances.size} instances",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Wrap the delete button in a Box for consistent placement
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 50.dp, end = 15.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        if (onDelete != null) {
                            ExtendedFloatingActionButton(
                                onClick = { showDialog.value = true },
                                shape = RoundedCornerShape(50),
                                containerColor = DeleteButtonColor,
                                contentColor = DeleteButtonTextColor,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Event",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                text = {
                                    Text(
                                        "Delete Event",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                    }
                }
                if (showDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showDialog.value = false },
                        containerColor = Gray800,
                        title = { Text("Delete Event") },
                        text = { Text("Are you sure you want to delete this event?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog.value = false
                                onDelete?.invoke()
                            }) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog.value = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                
                if (showDeleteDateDialog.value != null) {
                    val dateToDelete = showDeleteDateDialog.value!!
                    AlertDialog(
                        onDismissRequest = { showDeleteDateDialog.value = null },
                        containerColor = Gray800,
                        title = { Text("Delete Date") },
                        text = { 
                            Text("Are you sure you want to delete ${dateToDelete.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}?")
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                onDeleteDate?.invoke(dateToDelete)
                                showDeleteDateDialog.value = null
                            }) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDateDialog.value = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
        // Always show AddEventBottomSheet if requested
        if (showAddInstanceSheet.value) {
            AddEventBottomSheet(
                onDismiss = { showAddInstanceSheet.value = false },
                onSave = { name, date, note ->
                    if (date != null && viewModel != null) {
                        viewModel.addInstanceToEvent(
                            event.id,
                            name,
                            EventInstance(date, note)
                        )
                    } else if (date != null) {
                        onUpdateNote?.invoke(date, note ?: "")
                    }
                    showAddInstanceSheet.value = false
                },
                initialName = event.name,
                initialDate = java.time.LocalDate.now(),
                title = event.name,
                buttonLabel = "Save",
                editableName = false,
                showDateField = true,
                dateFieldLabel = "New Instance",
                allInstanceDates = event.instances.map { it.date }
            )
        }
    }
    // Bottom sheet for editing note
    if (showEditNoteSheet.value && editingNoteDate.value != null) {
        // --- Keyboard and focus logic ---
        val focusRequester = remember { FocusRequester() }
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
        androidx.compose.runtime.LaunchedEffect(showEditNoteSheet.value) {
            if (showEditNoteSheet.value) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
        ModalBottomSheet(
            onDismissRequest = {
                showEditNoteSheet.value = false
                editingNoteDate.value = null
            },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Gray800,
            tonalElevation = 4.dp,
            dragHandle = {},
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = if (initialNoteText.value.isBlank()) "Add note" else "Edit Note",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                OutlinedTextField(
                    value = editingNoteText.value,
                    onValueChange = { editingNoteText.value = it },
                    label = { Text("Note", color = White.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusedBorderColor,
                        unfocusedBorderColor = White.copy(alpha = 0.3f),
                        focusedLabelColor = FocusedLabelColor,
                        unfocusedLabelColor = White.copy(alpha = 0.7f),
                        cursorColor = CursorColor,
                        focusedTextColor = White,
                        unfocusedTextColor = White
                    ),
                    singleLine = false,
                    maxLines = 5
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            showEditNoteSheet.value = false
                            editingNoteDate.value = null
                        }
                    ) {
                        Text(
                            "Cancel",
                            color = White.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val date = editingNoteDate.value
                            if (date != null && onUpdateNote != null) {
                                onUpdateNote(date, editingNoteText.value)
                            }
                            showEditNoteSheet.value = false
                            editingNoteDate.value = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonContainerColor,
                            contentColor = Color.Black,
                            disabledContainerColor = ButtonDisabledColor,
                            disabledContentColor = Color.Black.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    // Add delete note dialog
    if (showDeleteNoteDialog.value != null) {
        val dateToDeleteNote = showDeleteNoteDialog.value!!
        AlertDialog(
            onDismissRequest = { showDeleteNoteDialog.value = null },
            containerColor = Gray800,
            title = { Text("Delete Note") },
            text = {
                Text("Are you sure you want to delete the note for ${dateToDeleteNote.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}?")
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateNote?.invoke(dateToDeleteNote, "")
                    showDeleteNoteDialog.value = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteNoteDialog.value = null }) {
                    Text("Cancel")
                }
            }
        )
    }
} 