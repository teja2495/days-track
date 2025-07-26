package com.tk.daystrack.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tk.daystrack.ui.theme.*

@Composable
fun AppHeader(
    title: String,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    showSettings: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Dimensions.headerPaddingTop, bottom = Dimensions.headerPaddingBottom),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = White,
            modifier = Modifier.padding(start = Dimensions.paddingSmall)
        )
        Spacer(modifier = Modifier.weight(1f))
        if (showSettings) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.padding(end = Dimensions.paddingSmall)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = White,
                    modifier = Modifier.size(Dimensions.iconSizeLarge)
                )
            }
        }
    }
} 