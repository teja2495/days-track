package com.tk.daystrack

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.platform.LocalContext
import com.tk.daystrack.components.*
import com.tk.daystrack.ui.theme.*
import java.time.LocalDate
import com.tk.daystrack.DateUtils.toTitleCase
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    event: Event, 
    onBack: () -> Unit, 
    onDelete: (() -> Unit)? = null,
    onDeleteDate: ((LocalDate) -> Unit)? = null,
    onUpdateNote: ((LocalDate, String) -> Unit)? = null,
    viewModel: EventViewModel? = null,
    fontSize: FontSize = FontSize.MEDIUM
) {
    val showDialog = remember { mutableStateOf(false) }
    val showDeleteDateDialog = remember { mutableStateOf<LocalDate?>(null) }
    val showEditNoteSheet = remember { mutableStateOf(false) }
    val editingNoteText = remember { mutableStateOf("") }
    val editingNoteDate = remember { mutableStateOf<LocalDate?>(null) }
    val initialNoteText = remember { mutableStateOf("") }
    val showDeleteNoteDialog = remember { mutableStateOf<LocalDate?>(null) }
    val context = LocalContext.current
    val repository = remember { EventRepository(context) }
    val showBanner = remember { mutableStateOf(false) }
    val showAddInstanceSheet = remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        showBanner.value = !repository.getHasSeenNoteHintBanner()
    }
    
    // Calculate sizes based on font size
    val titleFontSize = when (fontSize) {
        FontSize.SMALL -> 18.sp
        FontSize.MEDIUM -> 20.sp
        FontSize.LARGE -> 22.sp
    }
    
    val bodyFontSize = when (fontSize) {
        FontSize.SMALL -> 14.sp
        FontSize.MEDIUM -> 16.sp
        FontSize.LARGE -> 18.sp
    }
    
    val cardPadding = when (fontSize) {
        FontSize.SMALL -> 12.dp
        FontSize.MEDIUM -> 14.dp
        FontSize.LARGE -> 16.dp
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = event.name.toTitleCase(),
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = titleFontSize),
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
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
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = bodyFontSize),
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
                            HintBanner(
                                message = "Hint: Tap on the date card to add a note for that instance.",
                                onDismiss = {
                                    showBanner.value = false
                                    repository.setHasSeenNoteHintBanner(true)
                                }
                            )
                        }
                        
                        // Average frequency always visible
                        val sortedInstances = event.instances.sortedByDescending { it.date }
                        val avgFrequency = DateUtils.averageFrequency(sortedInstances.map { it.date })
                        if (avgFrequency != null) {
                            Column {
                                Row(
                                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Average frequency: ",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = bodyFontSize),
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "${"%.1f".format(avgFrequency)} days",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = bodyFontSize),
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
                                contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
                            ) {
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
                                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = titleFontSize),
                                                    fontWeight = FontWeight.Bold,
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
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = bodyFontSize),
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
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = bodyFontSize),
                                                    color = ThemeTextColor,
                                                    modifier = Modifier.padding(top = 0.dp),
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
                            DeleteEventFAB(onClick = { showDialog.value = true })
                        }
                    }
                }
            }
        }
        
        // Delete dialogs - moved outside the if-else block
        if (showDialog.value) {
            ConfirmationDialog(
                onDismiss = { showDialog.value = false },
                onConfirm = {
                    showDialog.value = false
                    onDelete?.invoke()
                },
                title = "Delete Event",
                message = "Are you sure you want to delete this event?",
                confirmText = "Delete"
            )
        }
        
        if (showDeleteDateDialog.value != null) {
            val dateToDelete = showDeleteDateDialog.value!!
            ConfirmationDialog(
                onDismiss = { showDeleteDateDialog.value = null },
                onConfirm = {
                    onDeleteDate?.invoke(dateToDelete)
                    showDeleteDateDialog.value = null
                },
                title = "Delete Date",
                message = "Are you sure you want to delete ${dateToDelete.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}?",
                confirmText = "Delete"
            )
        }
        
        if (showDeleteNoteDialog.value != null) {
            val dateToDeleteNote = showDeleteNoteDialog.value!!
            ConfirmationDialog(
                onDismiss = { showDeleteNoteDialog.value = null },
                onConfirm = {
                    onUpdateNote?.invoke(dateToDeleteNote, "")
                    showDeleteNoteDialog.value = null
                },
                title = "Delete Note",
                message = "Are you sure you want to delete the note for ${dateToDeleteNote.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}?",
                confirmText = "Delete"
            )
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
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        
        LaunchedEffect(showEditNoteSheet.value) {
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
                
                StyledOutlinedTextField(
                    value = editingNoteText.value,
                    onValueChange = { editingNoteText.value = it },
                    label = "Note",
                    modifier = Modifier.fillMaxWidth(),
                    focusRequester = focusRequester,
                    singleLine = false,
                    maxLines = 5
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryButton(
                        onClick = {
                            showEditNoteSheet.value = false
                            editingNoteDate.value = null
                        },
                        text = "Cancel"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PrimaryButton(
                        onClick = {
                            val date = editingNoteDate.value
                            if (date != null && onUpdateNote != null) {
                                onUpdateNote(date, editingNoteText.value)
                            }
                            showEditNoteSheet.value = false
                            editingNoteDate.value = null
                        },
                        text = "Save"
                    )
                }
            }
        }
    }
} 