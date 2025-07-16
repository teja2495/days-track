package com.tk.daystrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tk.daystrack.ui.theme.DarkerSurfaceVariant
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import com.tk.daystrack.DateUtils.toTitleCase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.clickable
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.tk.daystrack.ui.theme.EventDatePink

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
        mutableStateOf(prefs.getBoolean(PREF_KEY, false))
    }
    fun saveShowDaysOnly(value: Boolean) {
        prefs.edit().putBoolean(PREF_KEY, value).apply()
    }
    val timeDifference = DateUtils.formatTimeDifference(event.dates.last())
    val formattedDate = event.dates.last().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    val daysOnly = DateUtils.getDaysDifference(event.dates.last())
    val isFuture = event.dates.last().isAfter(java.time.LocalDate.now())
    val isToday = event.dates.last().isEqual(java.time.LocalDate.now())
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkerSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.name.toTitleCase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                val displayText = if (showDaysOnly) {
                    when {
                        isToday -> "today"
                        isFuture -> "$daysOnly days until"
                        else -> "$daysOnly days ago"
                    }
                } else timeDifference.replace(Regex("\\s*\\(\\d+ days?\\).*"), "")
                Text(
                    text = displayText.trim(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = EventDatePink,
                    modifier = Modifier
                        .clickable {
                            showDaysOnly = !showDaysOnly
                            saveShowDaysOnly(showDaysOnly)
                        }
                        .padding(bottom = 8.dp) // Add extra padding below
                )
            }
            IconButton(
                onClick = { onUpdate(event.copy(dates = event.dates + java.time.LocalDate.now())) },
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Update Event",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} 