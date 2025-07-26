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
import com.tk.daystrack.components.*
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateEventDialog(
    event: Event,
    onDismiss: () -> Unit,
    onUpdate: (String, LocalDate, String?) -> Unit,
    onDelete: (String) -> Unit
) {
    var eventName by remember { mutableStateOf(event.name) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var note by remember { mutableStateOf("") }
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = Shapes.bottomSheetShape,
        containerColor = Gray800,
        tonalElevation = Elevations.bottomSheet,
        sheetState = sheetState,
        dragHandle = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.bottomSheetPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingExtraLarge)
        ) {
            Text(
                text = context.getString(R.string.update_event_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = White
            )

            StyledOutlinedTextField(
                value = eventName,
                onValueChange = { eventName = it },
                label = context.getString(R.string.update_event_name_label),
                modifier = Modifier.fillMaxWidth(),
                focusRequester = focusRequester
            )
            
            StyledOutlinedTextField(
                value = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                onValueChange = { },
                label = context.getString(R.string.update_event_date_label),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showDatePicker = true }) {
                    Text(context.getString(R.string.add_event_date_label), color = ThemeTextColor)
                }
            }
            
            StyledOutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = context.getString(R.string.update_event_note_label),
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onDelete(event.id) }
                ) {
                    Text(
                        context.getString(R.string.update_event_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Row {
                    SecondaryButton(onClick = onDismiss, text = context.getString(R.string.action_cancel))
                    Spacer(modifier = Modifier.width(Dimensions.spacingMedium))
                    PrimaryButton(
                        onClick = {
                            if (eventName.isNotBlank()) {
                                onUpdate(eventName, selectedDate, note.ifBlank { null })
                            }
                        },
                        enabled = eventName.isNotBlank(),
                        text = context.getString(R.string.update_event_update)
                    )
                }
            }
        }
    }

    // Request focus when the bottom sheet is displayed
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    if (showDatePicker) {
        StyledDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate = it },
            initialDate = selectedDate
        )
    }
} 