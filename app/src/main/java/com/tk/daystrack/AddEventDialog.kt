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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.tk.daystrack.ui.theme.*
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String, LocalDate) -> Unit,
    initialName: String = "",
    initialDate: LocalDate = LocalDate.now(),
    title: String = "Add New Event",
    buttonLabel: String = "Save",
    editableName: Boolean = true,
    dateFieldLabel: String = "Event Date"
) {
    var eventName by remember { mutableStateOf(initialName) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

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

            if (editableName) {
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Event Name", color = White.copy(alpha = 0.7f)) },
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
                                onSave(eventName, selectedDate)
                            }
                        } else {
                            onSave(initialName, selectedDate)
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