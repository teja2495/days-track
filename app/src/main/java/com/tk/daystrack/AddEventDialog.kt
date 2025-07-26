package com.tk.daystrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.tk.daystrack.ui.theme.*
import com.tk.daystrack.components.*
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String, LocalDate?, String?) -> Unit,
    initialName: String = "",
    initialDate: LocalDate = LocalDate.now(),
    title: String = "New Event",
    buttonLabel: String = "Save",
    editableName: Boolean = true,
    showDateField: Boolean = false,
    dateFieldLabel: String = "Event Date",
    allInstanceDates: List<LocalDate> = emptyList()
) {
    var eventName by remember { mutableStateOf(initialName) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var note by remember { mutableStateOf("") }
    var noteFieldFocused by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Gray800,
        tonalElevation = 4.dp,
        sheetState = sheetState,
        dragHandle = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = White
            )

            if (showDateField && allInstanceDates.isNotEmpty() && !noteFieldFocused) {
                TimelineInstanceDates3(
                    allDates = allInstanceDates,
                    selectedDate = selectedDate
                )
            }

            if (editableName) {
                StyledOutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = "Name",
                    modifier = Modifier.fillMaxWidth(),
                    focusRequester = focusRequester
                )
            }
            
            if (showDateField) {
                StyledOutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    onValueChange = { },
                    label = dateFieldLabel,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("Select Date", color = ThemeTextColor)
                    }
                }
                
                StyledOutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Note (optional)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { noteFieldFocused = it.isFocused },
                    singleLine = false,
                    maxLines = 3
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecondaryButton(onClick = onDismiss, text = "Cancel")
                Spacer(modifier = Modifier.width(8.dp))
                PrimaryButton(
                    onClick = {
                        if (editableName) {
                            if (eventName.isNotBlank()) {
                                onSave(eventName, if (showDateField) selectedDate else null, note.ifBlank { null })
                            }
                        } else {
                            onSave(initialName, if (showDateField) selectedDate else null, note.ifBlank { null })
                        }
                    },
                    enabled = if (editableName) eventName.isNotBlank() else true,
                    text = buttonLabel
                )
            }
        }
    }

    // Request focus when the bottom sheet is displayed
    LaunchedEffect(Unit) {
        if (editableName) focusRequester.requestFocus()
    }

    if (showDatePicker) {
        StyledDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate = it },
            initialDate = selectedDate
        )
    }
}

// Timeline composable for AddEventBottomSheet
@Composable
fun TimelineInstanceDates(previousDate: LocalDate, selectedDate: LocalDate) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val isFuture = selectedDate.isAfter(previousDate)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isFuture) {
            TimelineDateItem(date = selectedDate, label = "New Instance", highlight = true)
            TimelineConnector()
            TimelineDateItem(date = previousDate, label = "Previous Instance")
        } else {
            TimelineDateItem(date = previousDate, label = "Previous Instance")
            TimelineConnector()
            TimelineDateItem(date = selectedDate, label = "New Instance", highlight = true)
        }
    }
}

@Composable
fun TimelineDateItem(date: LocalDate, label: String, highlight: Boolean = false, isMiddle: Boolean = false) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = date.format(formatter),
            color = if (highlight) ThemeTextColor else White,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun TimelineConnector() {
    Box(
        modifier = Modifier
            .width(4.dp)
            .height(40.dp)
            .padding(vertical = 12.dp)
            .background(
                White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(50)
            )
    )
} 

// Add new composable for 3-item timeline
@Composable
fun TimelineInstanceDates3(allDates: List<LocalDate>, selectedDate: LocalDate) {
    val sorted = allDates.sorted()
    val previous = sorted.filter { it.isBefore(selectedDate) }.maxOrNull()
    val next = sorted.filter { it.isAfter(selectedDate) }.minOrNull()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (previous != null) {
            TimelineDateItem(date = previous, label = "Previous", highlight = false, isMiddle = false)
            TimelineConnector()
        }
        TimelineDateItem(date = selectedDate, label = "New", highlight = true, isMiddle = true)
        if (next != null) {
            TimelineConnector()
            TimelineDateItem(date = next, label = "Next", highlight = false, isMiddle = false)
        }
    }
} 