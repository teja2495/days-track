package com.tk.daystrack.components

import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tk.daystrack.Event
import com.tk.daystrack.EventListItem
import com.tk.daystrack.FontSize
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventList(
    events: List<Event>,
    isEditMode: Boolean,
    reorderableState: ReorderableLazyListState?,
    onEventClick: (String) -> Unit,
    onEventLongPress: () -> Unit,
    onEventUpdate: (Event) -> Unit,
    onEventDelete: (String) -> Unit,
    onUpdateEventName: ((String, String) -> Unit)? = null,
    fontSize: FontSize,
    modifier: Modifier = Modifier,
    showAddButton: Boolean = true,
    onQuickAdd: ((Event) -> Unit)? = null
) {
    // Pre-calculate existing event names to avoid repeated calculations
    val existingEventNames = remember(events) { events.map { it.name } }
    
    // Create a stable list state for better performance
    val listState = rememberLazyListState()
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            LazyColumn(
                modifier = if (isEditMode && reorderableState != null) {
                    Modifier.fillMaxWidth().reorderable(reorderableState)
                } else {
                    Modifier.fillMaxWidth()
                },
                state = reorderableState?.listState ?: listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                // Performance optimizations for smoother scrolling
                flingBehavior = rememberSnapFlingBehavior(
                    lazyListState = reorderableState?.listState ?: listState
                ),
                // Add content padding for better scroll experience
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 140.dp
                ),
                // Optimize for better performance
                userScrollEnabled = true
            ) {
                items(
                    items = events,
                    key = { it.id },
                    contentType = { "event_item" }
                ) { event ->
                    val index = events.indexOf(event)
                    val isLastItem = index == events.size - 1
                    val itemModifier = if (isLastItem) Modifier.padding(bottom = 8.dp) else Modifier

                    if (isEditMode && reorderableState != null) {
                        org.burnoutcrew.reorderable.ReorderableItem(reorderableState, key = event.id) { isDragging ->
                            EventListItem(
                                event = event,
                                onUpdate = onEventUpdate,
                                onClick = null,
                                onLongPress = null,
                                modifier = itemModifier,
                                editMode = true,
                                reorderableState = reorderableState,
                                onDelete = { onEventDelete(event.id) },
                                onUpdateEventName = onUpdateEventName,
                                index = index,
                                fontSize = fontSize,
                                existingEventNames = existingEventNames,
                                showAddButton = showAddButton,
                                onQuickAdd = onQuickAdd
                            )
                        }
                    } else {
                        EventListItem(
                            event = event,
                            onUpdate = onEventUpdate,
                            onClick = { onEventClick(event.id) },
                            onLongPress = onEventLongPress,
                            modifier = itemModifier,
                            editMode = false,
                            reorderableState = null,
                            onUpdateEventName = onUpdateEventName,
                            index = index,
                            fontSize = fontSize,
                            existingEventNames = existingEventNames,
                            showAddButton = showAddButton,
                            onQuickAdd = onQuickAdd
                        )
                    }
                }
            }
        }
    }
}