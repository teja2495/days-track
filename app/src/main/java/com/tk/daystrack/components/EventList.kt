package com.tk.daystrack.components

import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tk.daystrack.Event
import com.tk.daystrack.EventListItem
import com.tk.daystrack.FontSize
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import androidx.compose.foundation.ExperimentalFoundationApi

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
    modifier: Modifier = Modifier
) {
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
                state = reorderableState?.listState ?: androidx.compose.foundation.lazy.rememberLazyListState(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                // Performance optimizations
                flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                    lazyListState = reorderableState?.listState ?: androidx.compose.foundation.lazy.rememberLazyListState()
                )
            ) {
                items(events.size, key = { events[it].id }) { index ->
                    val event = events[index]
                    val isLastItem = index == events.size - 1
                    val itemModifier = if (isLastItem) Modifier.padding(bottom = 140.dp) else Modifier

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
                                fontSize = fontSize
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
                            fontSize = fontSize
                        )
                    }
                }
            }
        }
    }
}