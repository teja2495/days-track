package com.tk.daystrack

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tk.daystrack.DateUtils.toTitleCase
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import java.util.UUID
import java.time.LocalDate
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.tk.daystrack.ui.theme.DayTrackBackgroundBrush
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dates: List<LocalDate>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    event: Event, 
    onBack: () -> Unit, 
    onDelete: (() -> Unit)? = null,
    onDeleteDate: ((LocalDate) -> Unit)? = null
) {
    val showDialog = remember { mutableStateOf(false) }
    val showDeleteDateDialog = remember { mutableStateOf<LocalDate?>(null) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = event.name.toTitleCase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(0f)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .zIndex(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val sortedDates = event.dates.sortedDescending()
                    val avgFrequency = DateUtils.averageFrequency(event.dates)
                    if (avgFrequency != null) {
                        item {
                            Text(
                                text = "Average frequency: ${"%.1f".format(avgFrequency)} days",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                            )
                        }
                    }
                    items(sortedDates.size) { index ->
                        val date = sortedDates[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (onDeleteDate != null && sortedDates.size > 1) {
                                    IconButton(
                                        onClick = { showDeleteDateDialog.value = date }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Date",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        // Add day interval text between consecutive dates
                        if (index < sortedDates.size - 1) {
                            val currentDate = date
                            val nextDate = sortedDates[index + 1]
                            val daysBetween = nextDate.toEpochDay() - currentDate.toEpochDay()
                            val intervalText = when {
                                daysBetween == -1L -> "1 day earlier"
                                daysBetween < -1L -> "${-daysBetween} days earlier"
                                daysBetween == 0L -> "Same day"
                                else -> "$daysBetween days later"
                            }
                            Text(
                                text = intervalText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, end = 8.dp, top = 10.dp),
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
                if (onDelete != null) {
                    Button(
                        onClick = { showDialog.value = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Event", color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text("Delete Event") },
                    text = { Text("Are you sure you want to delete this event?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog.value = false
                            onDelete?.invoke()
                        }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog.value = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            
            if (showDeleteDateDialog.value != null) {
                val dateToDelete = showDeleteDateDialog.value!!
                AlertDialog(
                    onDismissRequest = { showDeleteDateDialog.value = null },
                    title = { Text("Delete Date") },
                    text = { Text("Are you sure you want to delete ${dateToDelete.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}?") },
                    confirmButton = {
                        TextButton(onClick = {
                            onDeleteDate?.invoke(dateToDelete)
                            showDeleteDateDialog.value = null
                        }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDateDialog.value = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
} 