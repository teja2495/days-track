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
    index: Int = 0,
    fontSize: FontSize = FontSize.MEDIUM
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("event_list_item_prefs", Context.MODE_PRIVATE) }
    val PREF_KEY = "showDaysOnlyV2"
    var showDaysOnly by remember {
        mutableStateOf(prefs.getBoolean(PREF_KEY, false))
    }
    
    fun saveShowDaysOnly(value: Boolean) {
        prefs.edit().putBoolean(PREF_KEY, value).apply()
    }
    
    // Check if event has instances
    val hasInstances = event.instances.isNotEmpty()
    val lastInstance = event.instances.lastOrNull()
    val timeDifference = if (hasInstances) DateUtils.formatTimeDifference(lastInstance!!.date) else ""
    val daysOnly = if (hasInstances) DateUtils.getDaysDifference(lastInstance!!.date) else 0
    val isFuture = if (hasInstances) lastInstance!!.date.isAfter(java.time.LocalDate.now()) else false
    val isToday = if (hasInstances) lastInstance!!.date.isEqual(java.time.LocalDate.now()) else false
    
    var showEditNameSheet by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(event.name) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Calculate sizes based on font size
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
                .padding(cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (editMode) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = "Drag Handle",
                    tint = White,
                    modifier = Modifier
                        .size(dragHandleSize)
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
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = titleFontSize),
                        fontWeight = FontWeight.Bold,
                        color = ThemeTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { showEditNameSheet = true }
                    )
                } else {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = titleFontSize),
                        fontWeight = FontWeight.Bold,
                        color = White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (hasInstances && !editMode) {
                    val canToggle = DateUtils.isAtLeastOneMonth(lastInstance!!.date)
                    val displayText = if (showDaysOnly) {
                        when {
                            isToday -> "today"
                            isFuture -> "in $daysOnly days"
                            else -> "$daysOnly days ago"
                        }
                    } else {
                        timeDifference
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = if (canToggle) Modifier.clickable {
                            showDaysOnly = !showDaysOnly
                            saveShowDaysOnly(showDaysOnly)
                        } else Modifier
                    ) {
                        Icon(
                            imageVector = if (isFuture) Icons.Default.Schedule else Icons.Default.CalendarToday,
                            contentDescription = if (isFuture) "Clock" else "Calendar",
                            tint = DateTextColor,
                            modifier = Modifier.size(iconSize)
                        )
                        Text(
                            text = displayText,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = dateFontSize,
                                lineHeight = dateFontSize * 1.5f,
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
                        .size(buttonSize)
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Event",
                        tint = DeleteButtonColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Surface(
                    shape = CircleShape,
                    color = ButtonColor,
                    modifier = Modifier.size(buttonSize)
                ) {
                    IconButton(
                        onClick = { onUpdate(event) },
                        modifier = Modifier.size(buttonSize)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Update Event",
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
        
        LaunchedEffect(showEditNameSheet) {
            if (showEditNameSheet) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
        
        ModalBottomSheet(
            onDismissRequest = { showEditNameSheet = false },
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
                    text = "Edit Event Name",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                
                StyledOutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = "Name",
                    modifier = Modifier.fillMaxWidth(),
                    focusRequester = focusRequester
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryButton(onClick = { showEditNameSheet = false }, text = "Cancel")
                    Spacer(modifier = Modifier.width(8.dp))
                    PrimaryButton(
                        onClick = {
                            val trimmed = editedName.trim()
                            if (trimmed.isNotBlank()) {
                                showEditNameSheet = false
                                onUpdate(event.copy(name = trimmed.toTitleCase()))
                            }
                        },
                        enabled = editedName.trim().isNotBlank(),
                        text = "Save"
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
            title = "Delete Event",
            message = "Are you sure you want to delete '${event.name}'?",
            confirmText = "Delete"
        )
    }
} 