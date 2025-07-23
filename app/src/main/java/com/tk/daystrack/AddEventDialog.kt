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
import androidx.compose.ui.graphics.Color
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

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = java.time.ZoneId.systemDefault()
            .let { selectedDate.atStartOfDay(it).toInstant().toEpochMilli() }
    )

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
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Name", color = White.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal400,
                        unfocusedBorderColor = White.copy(alpha = 0.3f),
                        focusedLabelColor = Teal400,
                        unfocusedLabelColor = White.copy(alpha = 0.7f),
                        cursorColor = Teal400,
                        focusedTextColor = White,
                        unfocusedTextColor = White
                    ),
                    singleLine = true
                )
            }
            if (showDateField) {
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    onValueChange = { },
                    label = { Text(dateFieldLabel, color = White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        TextButton(
                            onClick = { showDatePicker = true }
                        ) {
                            Text("Select", color = Teal400)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal400,
                        unfocusedBorderColor = White.copy(alpha = 0.3f),
                        focusedLabelColor = Teal400,
                        unfocusedLabelColor = White.copy(alpha = 0.7f),
                        focusedTextColor = White,
                        unfocusedTextColor = White
                    )
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)", color = White.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { noteFieldFocused = it.isFocused },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal400,
                        unfocusedBorderColor = White.copy(alpha = 0.3f),
                        focusedLabelColor = Teal400,
                        unfocusedLabelColor = White.copy(alpha = 0.7f),
                        cursorColor = Teal400,
                        focusedTextColor = White,
                        unfocusedTextColor = White
                    ),
                    singleLine = false,
                    maxLines = 3
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        "Cancel",
                        color = White.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal500,
                        contentColor = Color.Black,
                        disabledContainerColor = Teal500.copy(alpha = 0.5f),
                        disabledContentColor = Color.Black.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(buttonLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Request focus when the bottom sheet is displayed
    LaunchedEffect(Unit) {
        if (editableName) focusRequester.requestFocus()
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            colors = DatePickerDefaults.colors(
                containerColor = Gray800, 
                titleContentColor = White,
                headlineContentColor = White,
                weekdayContentColor = White.copy(alpha = 0.7f),
                subheadContentColor = White,
                yearContentColor = White,
                currentYearContentColor = Teal400,
                selectedYearContentColor = White,
                selectedYearContainerColor = Teal500,
                dayContentColor = White,
                todayContentColor = Teal400,
                todayDateBorderColor = Teal400
            ),
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = java.time.LocalDate.ofInstant(
                                java.time.Instant.ofEpochMilli(millis),
                                java.time.ZoneOffset.UTC
                            )
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = Teal400)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel", color = White.copy(alpha = 0.7f))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Gray800,
                    selectedDayContentColor = Color.Black,
                    selectedDayContainerColor = Teal500,
                    dayContentColor = White,
                    todayContentColor = Teal400,
                    todayDateBorderColor = Teal400
                )
            )
        }
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
            color = if (highlight) Teal400 else White,
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