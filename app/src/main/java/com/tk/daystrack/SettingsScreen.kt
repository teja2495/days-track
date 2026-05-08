package com.tk.daystrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TextFields
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
    var sortDialogOpen by remember { mutableStateOf(false) }
    var fontDialogOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val versionName = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: ""
    } catch (e: Exception) {
        ""
    }

    val openFeedbackEmail = {
        val androidVersion = android.os.Build.VERSION.RELEASE
        val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        val emailBody = context.getString(R.string.feedback_body_template, versionName, androidVersion, deviceModel)
        val subject = context.getString(R.string.feedback_subject)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${context.getString(R.string.feedback_email)}?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(emailBody))
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Ignore if no email app is installed.
        }
    }

    val openAuthorLink = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://teja2495.github.io/teja-karlapudi-links/"))
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Ignore if no browser is installed.
        }
    }

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
                fontWeight = FontWeight.SemiBold,
                color = White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Gray800)
            ) {
                Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
                    Text(
                        text = context.getString(R.string.settings_data_backup),
                        style = MaterialTheme.typography.titleMedium,
                        color = White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = context.getString(R.string.settings_data_backup_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
                    ) {
                        OutlinedButton(
                            onClick = onImportClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(BorderColor)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = context.getString(R.string.settings_import),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(context.getString(R.string.settings_import), fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = {
                                if (hasEvents) {
                                    onExportClick()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.settings_export_no_events_toast), Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(BorderColor)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = context.getString(R.string.settings_export),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(context.getString(R.string.settings_export), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Gray800)
            ) {
                Column {
                    SettingsMenuRow(
                        title = context.getString(R.string.settings_sort_events),
                        subtitle = when (currentSortOption) {
                            SortOption.CUSTOM -> context.getString(R.string.sort_custom)
                            SortOption.DATE_ASCENDING -> context.getString(R.string.sort_date_oldest_first)
                            SortOption.DATE_DESCENDING -> context.getString(R.string.sort_date_newest_first)
                            SortOption.ALPHABETICAL -> context.getString(R.string.sort_alphabetical)
                        },
                        icon = Icons.Default.Sort,
                        onClick = { sortDialogOpen = true }
                    )
                    HorizontalDivider(color = Gray700.copy(alpha = 0.4f))
                    SettingsMenuRow(
                        title = context.getString(R.string.settings_font_card_size),
                        subtitle = when (currentFontSize) {
                            FontSize.SMALL -> context.getString(R.string.settings_font_small)
                            FontSize.MEDIUM -> context.getString(R.string.settings_font_medium)
                            FontSize.LARGE -> context.getString(R.string.settings_font_large)
                        },
                        icon = Icons.Default.TextFields,
                        onClick = { fontDialogOpen = true }
                    )
                    HorizontalDivider(color = Gray700.copy(alpha = 0.4f))
                    SettingsMenuRow(
                        title = context.getString(R.string.settings_send_feedback),
                        subtitle = context.getString(R.string.settings_send_feedback_subtitle),
                        icon = Icons.Default.MailOutline,
                        onClick = openFeedbackEmail,
                        showDivider = false
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
                ,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = context.getString(R.string.settings_version_footer, versionName),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = context.getString(R.string.settings_made_by_prefix),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = context.getString(R.string.settings_made_by_name),
                        style = MaterialTheme.typography.titleSmall,
                        color = ThemeTextColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .clickable(onClick = openAuthorLink)
                    )
                }
            }
        }
    }

    if (sortDialogOpen) {
        var tempSelected by remember { mutableStateOf(currentSortOption) }
        val sortOptions = listOf(
            SortOption.CUSTOM to SortOptionInfo(context.getString(R.string.sort_custom), context.getString(R.string.sort_custom_desc)),
            SortOption.DATE_ASCENDING to SortOptionInfo(context.getString(R.string.sort_date_oldest_first), context.getString(R.string.sort_date_oldest_desc)),
            SortOption.DATE_DESCENDING to SortOptionInfo(context.getString(R.string.sort_date_newest_first), context.getString(R.string.sort_date_newest_desc)),
            SortOption.ALPHABETICAL to SortOptionInfo(context.getString(R.string.sort_alphabetical), context.getString(R.string.sort_alphabetical_desc))
        )
        AlertDialog(
            onDismissRequest = { sortDialogOpen = false },
            containerColor = Gray800,
            title = { Text(context.getString(R.string.sort_choose_title), color = White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    sortOptions.forEach { (option, info) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tempSelected = option }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = tempSelected == option,
                                onClick = { tempSelected = option },
                                colors = RadioButtonDefaults.colors(selectedColor = ThemeTextColor, unselectedColor = TextSecondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(info.label, color = White)
                                Text(info.description, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSortOptionSelected(tempSelected)
                    sortDialogOpen = false
                }) { Text(context.getString(R.string.settings_ok), color = ThemeTextColor) }
            },
            dismissButton = {
                TextButton(onClick = { sortDialogOpen = false }) { Text(context.getString(R.string.settings_cancel), color = TextSecondary) }
            }
        )
    }

    if (fontDialogOpen) {
        var tempSelected by remember { mutableStateOf(currentFontSize) }
        val fontSizeOptions = listOf(
            FontSize.SMALL to context.getString(R.string.settings_font_small),
            FontSize.MEDIUM to context.getString(R.string.settings_font_medium),
            FontSize.LARGE to context.getString(R.string.settings_font_large)
        )
        AlertDialog(
            onDismissRequest = { fontDialogOpen = false },
            containerColor = Gray800,
            title = { Text(context.getString(R.string.settings_font_choose_title), color = White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    fontSizeOptions.forEach { (option, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tempSelected = option }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = tempSelected == option,
                                onClick = { tempSelected = option },
                                colors = RadioButtonDefaults.colors(selectedColor = ThemeTextColor, unselectedColor = TextSecondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, color = White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onFontSizeSelected(tempSelected)
                    fontDialogOpen = false
                }) { Text(context.getString(R.string.settings_ok), color = ThemeTextColor) }
            },
            dismissButton = {
                TextButton(onClick = { fontDialogOpen = false }) { Text(context.getString(R.string.settings_cancel), color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun SettingsMenuRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimensions.paddingLarge, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = title,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
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
