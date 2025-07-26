package com.tk.daystrack.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.tk.daystrack.ui.theme.*
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate = LocalDate.now()
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = ZoneId.systemDefault()
            .let { initialDate.atStartOfDay(it).toInstant().toEpochMilli() }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(
            containerColor = Gray800,
            titleContentColor = White,
            headlineContentColor = White,
            weekdayContentColor = White.copy(alpha = 0.7f),
            subheadContentColor = White,
            yearContentColor = White,
            currentYearContentColor = CalendarYearContentColor,
            selectedYearContentColor = White,
            selectedYearContainerColor = CalendarSelectedColor,
            dayContentColor = White,
            todayContentColor = CalendarTodayColor,
            todayDateBorderColor = CalendarTodayBorderColor
        ),
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = LocalDate.ofInstant(
                            java.time.Instant.ofEpochMilli(millis),
                            java.time.ZoneOffset.UTC
                        )
                        onDateSelected(selectedDate)
                    }
                    onDismiss()
                }
            ) {
                Text("OK", color = ThemeTextColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = White.copy(alpha = 0.7f))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = Gray800,
                selectedDayContentColor = Color.Black,
                selectedDayContainerColor = CalendarSelectedColor,
                dayContentColor = White,
                todayContentColor = CalendarTodayColor,
                todayDateBorderColor = CalendarTodayBorderColor
            )
        )
    }
} 