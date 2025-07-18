package com.tk.daystrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.clickable
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.tk.daystrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListItem(
    event: Event,
    onUpdate: (Event) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("event_list_item_prefs", Context.MODE_PRIVATE) }
    val PREF_KEY = "showDaysOnly"
    var showDaysOnly by remember {
        mutableStateOf(prefs.getBoolean(PREF_KEY, true)) // Default to true to match design
    }
    fun saveShowDaysOnly(value: Boolean) {
        prefs.edit().putBoolean(PREF_KEY, value).apply()
    }
    val timeDifference = DateUtils.formatTimeDifference(event.dates.last())
    val daysOnly = DateUtils.getDaysDifference(event.dates.last())
    val isFuture = event.dates.last().isAfter(java.time.LocalDate.now())
    val isToday = event.dates.last().isEqual(java.time.LocalDate.now())
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Gray800
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val displayText = if (showDaysOnly) {
                    when {
                        isToday -> "today"
                        isFuture -> "in $daysOnly days"
                        else -> "$daysOnly days ago"
                    }
                } else {
                    timeDifference
                }
                
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Teal400,
                    modifier = Modifier.clickable {
                        showDaysOnly = !showDaysOnly
                        saveShowDaysOnly(showDaysOnly)
                    }
                )
            }
            
            // Add button with circle background
            Surface(
                shape = CircleShape,
                color = ButtonColor,
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(
                    onClick = { onUpdate(event.copy(dates = event.dates + java.time.LocalDate.now())) },
                    modifier = Modifier.size(40.dp)
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