package com.tk.daystrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.tk.daystrack.ui.theme.*
import androidx.compose.ui.graphics.Color
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
import com.tk.daystrack.DateUtils.toTitleCase
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.tk.daystrack.components.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventListItem(
    event: Event,
    onUpdate: (Event) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    editMode: Boolean = false,
    reorderableState: ReorderableLazyListState? = null,
    onDelete: (() -> Unit)? = null,
    onUpdateEventName: ((String, String) -> Unit)? = null,
    index: Int = 0,
    fontSize: FontSize = FontSize.MEDIUM,
    existingEventNames: List<String> = emptyList()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("event_list_item_prefs", Context.MODE_PRIVATE) }
    val PREF_KEY = remember(event.id) { "showDaysOnlyV2_${event.id}" }
    var showDaysOnly by remember {
        mutableStateOf(prefs.getBoolean(PREF_KEY, false))
    }
    
    fun saveShowDaysOnly(value: Boolean) {
        prefs.edit().putBoolean(PREF_KEY, value).apply()
    }
    
    // Pre-calculate all expensive values to avoid recomposition overhead
    val eventData = remember(event) {
        val hasInstances = event.instances.isNotEmpty()
        val latestInstance = if (hasInstances) event.instances.maxByOrNull { it.date } else null
        val timeDifference = if (hasInstances) DateUtils.formatTimeDifferenceCached(latestInstance!!.date) else ""
        val daysOnly = if (hasInstances) DateUtils.getDaysDifference(latestInstance!!.date) else 0
        val isFuture = if (hasInstances) latestInstance!!.date.isAfter(java.time.LocalDate.now()) else false
        val isToday = if (hasInstances) latestInstance!!.date.isEqual(java.time.LocalDate.now()) else false
        val canToggle = if (hasInstances) DateUtils.isAtLeastOneMonth(latestInstance!!.date) else false
        
        EventData(
            hasInstances = hasInstances,
            latestInstance = latestInstance,
            timeDifference = timeDifference,
            daysOnly = daysOnly,
            isFuture = isFuture,
            isToday = isToday,
            canToggle = canToggle
        )
    }
    
    // Pre-calculate sizes based on font size to avoid repeated calculations
    val sizes = remember(fontSize) {
        val cardPadding = when (fontSize) {
            FontSize.SMALL -> 16.dp
            FontSize.MEDIUM -> 20.dp
            FontSize.LARGE -> 24.dp
        }
        
        val titleFontSize = when (fontSize) {
            FontSize.SMALL -> 16.sp
            FontSize.MEDIUM -> 18.sp
            FontSize.LARGE -> 22.sp
        }
        
        val dateFontSize = when (fontSize) {
            FontSize.SMALL -> 12.sp
            FontSize.MEDIUM -> 14.sp
            FontSize.LARGE -> 16.sp
        }
        
        val iconSize = when (fontSize) {
            FontSize.SMALL -> 12.dp
            FontSize.MEDIUM -> 14.dp
            FontSize.LARGE -> 16.dp
        }
        
        val buttonSize = when (fontSize) {
            FontSize.SMALL -> 32.dp
            FontSize.MEDIUM -> 36.dp
            FontSize.LARGE -> 40.dp
        }
        
        val dragHandleSize = when (fontSize) {
            FontSize.SMALL -> 28.dp
            FontSize.MEDIUM -> 32.dp
            FontSize.LARGE -> 36.dp
        }
        
        Sizes(
            cardPadding = cardPadding,
            titleFontSize = titleFontSize,
            dateFontSize = dateFontSize,
            iconSize = iconSize,
            buttonSize = buttonSize,
            dragHandleSize = dragHandleSize
        )
    }
    
    var showEditNameSheet by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(event.name) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDuplicateNameError by remember { mutableStateOf(false) }
    var hasAttemptedSave by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .let {
                if (onClick != null || onLongPress != null) {
                    it.combinedClickable(
                        onClick = { onClick?.invoke() },
                        onLongClick = { onLongPress?.invoke() }
                    )
                } else it
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Gray800
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sizes.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (editMode) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = stringResource(R.string.event_list_item_drag_handle),
                    tint = White,
                    modifier = Modifier
                        .size(sizes.dragHandleSize)
                        .padding(end = 16.dp)
                        .let { base ->
                            if (reorderableState != null) base.detectReorder(reorderableState) else base
                        }
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (editMode) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = sizes.titleFontSize),
                        fontWeight = FontWeight.Bold,
                        color = ThemeTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { showEditNameSheet = true }
                    )
                } else {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = sizes.titleFontSize),
                        fontWeight = FontWeight.Bold,
                        color = White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (!editMode) {
                    if (eventData.hasInstances) {
                        val displayText = if (showDaysOnly) {
                            when {
                                eventData.isToday -> stringResource(R.string.event_list_item_today)
                                eventData.isFuture -> stringResource(R.string.event_list_item_in_days, eventData.daysOnly)
                                else -> stringResource(R.string.event_list_item_days_ago, eventData.daysOnly)
                            }
                        } else {
                            eventData.timeDifference
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = if (eventData.canToggle) Modifier.clickable {
                                showDaysOnly = !showDaysOnly
                                saveShowDaysOnly(showDaysOnly)
                            } else Modifier
                        ) {
                            Icon(
                                imageVector = if (eventData.isFuture) Icons.Default.Schedule else Icons.Default.CalendarToday,
                                contentDescription = if (eventData.isFuture) stringResource(R.string.event_list_item_clock) else stringResource(R.string.event_list_item_calendar),
                                tint = DateTextColor,
                                modifier = Modifier.size(sizes.iconSize)
                            )
                            Text(
                                text = displayText,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = sizes.dateFontSize,
                                    lineHeight = sizes.dateFontSize * 1.5f,
                                    fontStyle = FontStyle.Italic,
                                    color = DateTextColor
                                )
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.event_list_item_no_instance),
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = sizes.dateFontSize,
                                lineHeight = sizes.dateFontSize * 1.5f,
                                fontStyle = FontStyle.Italic,
                                color = DateTextColor
                            )
                        )
                    }
                }
            }
            
            // Trailing icon: + (normal) or delete (edit mode)
            if (editMode) {
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .size(sizes.buttonSize)
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.event_list_item_delete_event),
                        tint = DeleteButtonColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Surface(
                    shape = CircleShape,
                    color = ButtonColor,
                    modifier = Modifier.size(sizes.buttonSize)
                ) {
                    IconButton(
                        onClick = { onUpdate(event) },
                        modifier = Modifier.size(sizes.buttonSize)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.event_list_item_update_event),
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
    
    if (showEditNameSheet) {
        val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        
        LaunchedEffect(showEditNameSheet) {
            if (showEditNameSheet) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
        
        // Scroll to maximum when error appears
        LaunchedEffect(showDuplicateNameError) {
            if (showDuplicateNameError) {
                sheetState.expand()
            }
        }
        
        // Check for duplicate name when editedName changes (only if user has attempted to save)
        LaunchedEffect(editedName, hasAttemptedSave) {
            if (hasAttemptedSave) {
                val trimmedName = editedName.trim()
                // Exclude the current event from the duplicate check
                val otherEventNames = existingEventNames.filter { it != event.name }
                showDuplicateNameError = trimmedName.isNotBlank() && 
                    otherEventNames.any { it.equals(trimmedName, ignoreCase = true) }
            } else {
                showDuplicateNameError = false
            }
        }
        
        ModalBottomSheet(
            onDismissRequest = { showEditNameSheet = false },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Gray800,
            tonalElevation = 4.dp,
            dragHandle = {},
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.event_list_item_edit_name_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                
                StyledOutlinedTextField(
                    value = editedName,
                    onValueChange = { 
                        editedName = it
                        // Reset validation when user edits the name
                        if (hasAttemptedSave) {
                            hasAttemptedSave = false
                        }
                    },
                    label = stringResource(R.string.event_list_item_name_label),
                    modifier = Modifier.fillMaxWidth(),
                    focusRequester = focusRequester,
                    isError = showDuplicateNameError
                )
                
                // Show error message if duplicate name
                if (showDuplicateNameError) {
                    Text(
                        text = stringResource(R.string.add_event_duplicate_name_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryButton(onClick = { showEditNameSheet = false }, text = stringResource(R.string.event_list_item_cancel))
                    Spacer(modifier = Modifier.width(8.dp))
                    PrimaryButton(
                        onClick = {
                            val trimmed = editedName.trim()
                            if (trimmed.isNotBlank()) {
                                // Set hasAttemptedSave to true to trigger validation
                                hasAttemptedSave = true
                                
                                // Check for duplicate name (excluding current event)
                                val otherEventNames = existingEventNames.filter { it != event.name }
                                val isDuplicate = otherEventNames.any { it.equals(trimmed, ignoreCase = true) }
                                
                                if (!isDuplicate) {
                                    showEditNameSheet = false
                                    if (editMode && onUpdateEventName != null) {
                                        onUpdateEventName(event.id, trimmed.toTitleCase())
                                    } else {
                                        onUpdate(event.copy(name = trimmed.toTitleCase()))
                                    }
                                }
                            }
                        },
                        enabled = editedName.trim().isNotBlank(),
                        text = stringResource(R.string.event_list_item_save)
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        ConfirmationDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete?.invoke()
            },
            title = stringResource(R.string.event_list_item_delete_confirm_title),
            message = stringResource(R.string.event_list_item_delete_confirm_message, event.name),
            confirmText = stringResource(R.string.event_list_item_delete_confirm),
            isDeleteDialog = true
        )
    }
} 

// Data classes to hold pre-calculated values
private data class EventData(
    val hasInstances: Boolean,
    val latestInstance: EventInstance?,
    val timeDifference: String,
    val daysOnly: Long,
    val isFuture: Boolean,
    val isToday: Boolean,
    val canToggle: Boolean
)

private data class Sizes(
    val cardPadding: androidx.compose.ui.unit.Dp,
    val titleFontSize: androidx.compose.ui.unit.TextUnit,
    val dateFontSize: androidx.compose.ui.unit.TextUnit,
    val iconSize: androidx.compose.ui.unit.Dp,
    val buttonSize: androidx.compose.ui.unit.Dp,
    val dragHandleSize: androidx.compose.ui.unit.Dp
) 