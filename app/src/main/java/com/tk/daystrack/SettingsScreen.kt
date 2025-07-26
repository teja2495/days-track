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
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.tk.daystrack.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri

data class SortOptionInfo(
    val label: String,
    val description: String
)

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
                .padding(top = 12.dp, bottom = 32.dp),
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = White
            )
        }
        
        // Settings content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp) // Increased spacing between sections
        ) {
            // Sort Events Section
            Column {
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
            }

            // Data Management Section
            Column {
                Text(
                    text = "Events Data Backup",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onImportClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Gray800)
                    ) {
                        Text("Import", color = White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onExportClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Gray800)
                    ) {
                        Text("Export", color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Version and feedback section
        Spacer(modifier = Modifier.weight(1f))
        val context = LocalContext.current
        val versionName = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: ""
        } catch (e: Exception) {
            ""
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Version $versionName",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "  •  ",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "Send Feedback",
                style = MaterialTheme.typography.bodySmall,
                color = ThemeTextColor,
                modifier = Modifier.clickable {
                    val androidVersion = android.os.Build.VERSION.RELEASE
                    val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                    val emailBody = """
                        \n\n\n---\nApp Version: $versionName\nAndroid Version: $androidVersion\nDevice: $deviceModel
                    """.trimIndent()
                    val subject = "Days Track Feedback"
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:tejakarlapudi.apps@gmail.com?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(emailBody))
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Handle case where no email app is installed
                    }
                }
            )
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
    var isPressed by remember { mutableStateOf(false) }
    val sortOptions = listOf(
        SortOption.DATE_ASCENDING to SortOptionInfo("Date (Oldest First)", "Events from earliest to latest"),
        SortOption.DATE_DESCENDING to SortOptionInfo("Date (Newest First)", "Events from latest to earliest"),
        SortOption.ALPHABETICAL to SortOptionInfo("Alphabetical", "Events A to Z by name")
    )
    val selectedText = when (currentSortOption) {
        SortOption.DATE_ASCENDING -> "Date (Oldest First)"
        SortOption.DATE_DESCENDING -> "Date (Newest First)"
        SortOption.ALPHABETICAL -> "Alphabetical"
        SortOption.CUSTOM -> "Custom (Manual Order)"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = if (isPressed) 2.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .border(
                width = 1.dp,
                color = if (dialogOpen) ThemeTextColor else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Gray800
        ),
        onClick = { 
            dialogOpen = true
            isPressed = true
        },
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
                tint = if (dialogOpen) ThemeTextColor else White,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (dialogOpen) {
        var tempSelected by remember { mutableStateOf(currentSortOption) }
        AlertDialog(
            onDismissRequest = { 
                dialogOpen = false
                isPressed = false
            },
            containerColor = Gray800,
            titleContentColor = White,
            textContentColor = White,
            title = {
                Text("Choose Sort Option", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    sortOptions.forEach { (option, sortInfo) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable { tempSelected = option }
                        ) {
                            RadioButton(
                                selected = tempSelected == option,
                                onClick = { tempSelected = option },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = ThemeTextColor,
                                    unselectedColor = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Text content
                            Column {
                                Text(
                                    text = sortInfo.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (tempSelected == option) ThemeTextColor else White,
                                    fontWeight = if (tempSelected == option) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = sortInfo.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (tempSelected == option) ThemeTextColor.copy(alpha = 0.7f) else TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSortOptionSelected(tempSelected)
                    dialogOpen = false
                    isPressed = false
                }) {
                    Text("OK", color = ThemeTextColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    dialogOpen = false
                    isPressed = false
                }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
} 