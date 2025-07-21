package com.tk.daystrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.tk.daystrack.ui.theme.*

@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    currentSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier,
    onExportClick: () -> Unit = {},
    onImportClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray900)
            .padding(horizontal = 16.dp, vertical = 32.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,  // Changed from headlineLarge to headlineMedium
                fontWeight = FontWeight.Bold,
                color = White
            )
        }
        
        // Settings content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sort Events",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            CustomSortDropdown(
                currentSortOption = currentSortOption,
                onSortOptionSelected = onSortOptionSelected
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onExportClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Teal500)
            ) {
                Text("Export Data", color = White, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onImportClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Teal400)
            ) {
                Text("Import Data", color = Gray900, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSortDropdown(
    currentSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var dialogOpen by remember { mutableStateOf(false) }
    val sortOptions = listOf(
        SortOption.DATE_ASCENDING to "Date (Ascending)",
        SortOption.DATE_DESCENDING to "Date (Descending)",
        SortOption.ALPHABETICAL to "Alphabetical"
    )
    val selectedText = sortOptions.first { it.first == currentSortOption }.second

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Gray800
        ),
        onClick = { dialogOpen = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedText,
                style = MaterialTheme.typography.bodyLarge,
                color = White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Sort options",
                tint = White
            )
        }
    }

    if (dialogOpen) {
        var tempSelected by remember { mutableStateOf(currentSortOption) }
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            containerColor = Gray800,
            titleContentColor = White,
            textContentColor = White,
            title = {
                Text("Choose Sort Option", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    sortOptions.forEach { (option, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { tempSelected = option }
                        ) {
                            RadioButton(
                                selected = tempSelected == option,
                                onClick = { tempSelected = option },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Teal400,
                                    unselectedColor = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (tempSelected == option) Teal400 else White,
                                fontWeight = if (tempSelected == option) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSortOptionSelected(tempSelected)
                    dialogOpen = false
                }) {
                    Text("OK", color = Teal400, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                                    TextButton(onClick = { dialogOpen = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
            }
        )
    }
} 