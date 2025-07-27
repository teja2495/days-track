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
import android.widget.Toast
import com.tk.daystrack.components.*
import androidx.compose.ui.unit.dp

data class SortOptionInfo(
    val label: String,
    val description: String
)

@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    currentSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    currentFontSize: FontSize,
    onFontSizeSelected: (FontSize) -> Unit,
    modifier: Modifier = Modifier,
    onExportClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    hasEvents: Boolean = true
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray900)
            .padding(horizontal = Dimensions.paddingMedium, vertical = Dimensions.paddingExtraLarge)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.headerPaddingTop, bottom = Dimensions.paddingExtraLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = context.getString(R.string.cd_back),
                    tint = White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
            
            Text(
                text = context.getString(R.string.settings_title),
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
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)
        ) {
            // Sort Events Section
            Column {
                SectionTitle(text = context.getString(R.string.settings_sort_events))
                CustomSortDropdown(
                    currentSortOption = currentSortOption,
                    onSortOptionSelected = onSortOptionSelected
                )
            }

            // Font Size Section
            Column {
                SectionTitle(text = context.getString(R.string.settings_font_card_size))
                CustomFontSizeDropdown(
                    currentFontSize = currentFontSize,
                    onFontSizeSelected = onFontSizeSelected
                )
            }

            // Data Management Section
            Column {
                SectionTitle(text = context.getString(R.string.settings_data_backup))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
                ) {
                    Button(
                        onClick = onImportClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Gray800)
                    ) {
                        Text(context.getString(R.string.settings_import), color = White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (hasEvents) {
                                onExportClick()
                            } else {
                                Toast.makeText(context, context.getString(R.string.settings_export_no_events_toast), Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Gray800)
                    ) {
                        Text(context.getString(R.string.settings_export), color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Version and feedback section
        Spacer(modifier = Modifier.weight(1f))
        val versionName = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: ""
        } catch (e: Exception) {
            ""
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.paddingExtraLarge),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = context.getString(R.string.settings_version, versionName),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "  •  ",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = context.getString(R.string.settings_send_feedback),
                style = MaterialTheme.typography.bodySmall,
                color = ThemeTextColor,
                modifier = Modifier.clickable {
                    val androidVersion = android.os.Build.VERSION.RELEASE
                    val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                    val emailBody = context.getString(R.string.feedback_body_template, versionName, androidVersion, deviceModel)
                    val subject = context.getString(R.string.feedback_subject)
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${context.getString(R.string.feedback_email)}?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(emailBody))
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
    val context = LocalContext.current
    val sortOptions = listOf(
        SortOption.CUSTOM to SortOptionInfo(context.getString(R.string.sort_custom), context.getString(R.string.sort_custom_desc)),
        SortOption.DATE_ASCENDING to SortOptionInfo(context.getString(R.string.sort_date_oldest_first), context.getString(R.string.sort_date_oldest_desc)),
        SortOption.DATE_DESCENDING to SortOptionInfo(context.getString(R.string.sort_date_newest_first), context.getString(R.string.sort_date_newest_desc)),
        SortOption.ALPHABETICAL to SortOptionInfo(context.getString(R.string.sort_alphabetical), context.getString(R.string.sort_alphabetical_desc))
    )
    val selectedText = when (currentSortOption) {
        SortOption.DATE_ASCENDING -> context.getString(R.string.sort_date_oldest_first)
        SortOption.DATE_DESCENDING -> context.getString(R.string.sort_date_newest_first)
        SortOption.ALPHABETICAL -> context.getString(R.string.sort_alphabetical)
        SortOption.CUSTOM -> context.getString(R.string.sort_custom)
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
                contentDescription = context.getString(R.string.cd_sort_options),
                tint = if (dialogOpen) ThemeTextColor else White,
                modifier = Modifier.size(Dimensions.iconSizeMedium)
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
                Text(context.getString(R.string.sort_choose_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                    Text(context.getString(R.string.settings_ok), color = ThemeTextColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    dialogOpen = false
                    isPressed = false
                }) {
                    Text(context.getString(R.string.settings_cancel), color = TextSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFontSizeDropdown(
    currentFontSize: FontSize,
    onFontSizeSelected: (FontSize) -> Unit,
    modifier: Modifier = Modifier
) {
    var dialogOpen by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val fontSizeOptions = listOf(
        FontSize.SMALL to context.getString(R.string.settings_font_small),
        FontSize.MEDIUM to context.getString(R.string.settings_font_medium), 
        FontSize.LARGE to context.getString(R.string.settings_font_large)
    )
    val selectedText = when (currentFontSize) {
        FontSize.SMALL -> context.getString(R.string.settings_font_small)
        FontSize.MEDIUM -> context.getString(R.string.settings_font_medium)
        FontSize.LARGE -> context.getString(R.string.settings_font_large)
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
                contentDescription = context.getString(R.string.cd_font_size_options),
                tint = if (dialogOpen) ThemeTextColor else White,
                modifier = Modifier.size(Dimensions.iconSizeMedium)
            )
        }
    }

    if (dialogOpen) {
        var tempSelected by remember { mutableStateOf(currentFontSize) }
        AlertDialog(
            onDismissRequest = { 
                dialogOpen = false
                isPressed = false
            },
            containerColor = Gray800,
            titleContentColor = White,
            textContentColor = White,
            title = {
                Text(context.getString(R.string.settings_font_choose_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    fontSizeOptions.forEach { (option, label) ->
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
                            
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (tempSelected == option) ThemeTextColor else White,
                                fontWeight = if (tempSelected == option) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onFontSizeSelected(tempSelected)
                    dialogOpen = false
                    isPressed = false
                }) {
                    Text(context.getString(R.string.settings_ok), color = ThemeTextColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    dialogOpen = false
                    isPressed = false
                }) {
                    Text(context.getString(R.string.settings_cancel), color = TextSecondary)
                }
            }
        )
    }
} 