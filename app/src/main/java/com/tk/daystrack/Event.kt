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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import java.util.UUID
import java.time.LocalDate
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.tk.daystrack.ui.theme.*
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close

data class EventInstance(
    val date: LocalDate,
    val note: String? = null
)

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val instances: List<EventInstance>
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
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
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .zIndex(1f),
                verticalArrangement = Arrangement.Top
            ) {
                val showBanner = remember { mutableStateOf(true) }
                // Move banner to the top
                if (showBanner.value) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hint: Tap on a date to add a note for that instance",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(onClick = { showBanner.value = false }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val sortedInstances = event.instances.sortedByDescending { it.date }
                    val avgFrequency = DateUtils.averageFrequency(sortedInstances.map { it.date })
                    if (avgFrequency != null) {
                        item {
                            Column {
                                Row(
                                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Average frequency: ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "${"%.1f".format(avgFrequency)} days",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = White
                                    )
                                }
                            }
                        }
                    }
                    items(sortedInstances.size) { index ->
                        val instance = sortedInstances[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = instance.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (onDeleteDate != null) {
                                        IconButton(
                                            onClick = { showDeleteDateDialog.value = instance.date }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Date",
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                }
                                if (!instance.note.isNullOrBlank()) {
                                    Text(
                                        text = instance.note,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                                    )
                                }
                            }
                        }
                        // Add day interval text between consecutive instances
                        if (index < sortedInstances.size - 1) {
                            val currentDate = instance.date
                            val nextDate = sortedInstances[index + 1].date
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
                                color = com.tk.daystrack.ui.theme.Teal400,
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
                        colors = ButtonDefaults.buttonColors(containerColor = DeleteButtonColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text("Delete Event", color = DeleteButtonTextColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    containerColor = Gray800,
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
                    containerColor = Gray800,
                    title = { Text("Delete Date") },
                    text = { 
                        Text("Are you sure you want to delete ${dateToDelete.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}?")
                    },
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