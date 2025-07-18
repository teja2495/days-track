package com.tk.daystrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.tk.daystrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onSave: (String, LocalDate) -> Unit
) {
    var eventName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = java.time.ZoneId.systemDefault()
            .let { selectedDate.atStartOfDay(it).toInstant().toEpochMilli() }
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Gray800
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Add New Event",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                
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
                
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    onValueChange = { },
                    label = { Text("Event Date", color = White.copy(alpha = 0.7f)) },
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
                            if (eventName.isNotBlank()) {
                                onSave(eventName, selectedDate)
                            }
                        },
                        enabled = eventName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Teal500,
                            contentColor = White,
                            disabledContainerColor = Teal500.copy(alpha = 0.5f),
                            disabledContentColor = White.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    
    // Request focus when the dialog is displayed
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
                selectedDayContentColor = White,
                selectedDayContainerColor = Teal500,
                todayContentColor = Teal400,
                todayDateBorderColor = Teal400
            ),
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
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
            DatePicker(state = datePickerState)
        }
    }
} 