package com.tk.daystrack.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.tk.daystrack.ui.theme.*

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    text: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = ButtonContainerColor,
            contentColor = Color.Black,
            disabledContainerColor = ButtonDisabledColor,
            disabledContentColor = Color.Black.copy(alpha = 0.7f)
        ),
        shape = Shapes.buttonShape
    ) {
        icon?.invoke()
        if (icon != null) {
            Spacer(modifier = Modifier.width(Dimensions.spacingMedium))
        }
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text, color = White.copy(alpha = 0.7f))
    }
}

@Composable
fun DangerButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun ConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Gray800,
        title = { 
            Text(
                title, 
                color = White, 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold
            ) 
        },
        text = { Text(message, color = White) },
        confirmButton = {
            DangerButton(onClick = onConfirm, text = confirmText)
        },
        dismissButton = {
            SecondaryButton(onClick = onDismiss, text = dismissText)
        },
        shape = Shapes.dialogShape
    )
}

@Composable
fun StyledOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    focusRequester: androidx.compose.ui.focus.FocusRequester? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = White.copy(alpha = 0.7f)) },
        modifier = modifier.let { 
            if (focusRequester != null) it.focusRequester(focusRequester) else it 
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FocusedBorderColor,
            unfocusedBorderColor = White.copy(alpha = 0.3f),
            focusedLabelColor = FocusedLabelColor,
            unfocusedLabelColor = White.copy(alpha = 0.7f),
            cursorColor = CursorColor,
            focusedTextColor = White,
            unfocusedTextColor = White
        ),
        singleLine = singleLine,
        maxLines = maxLines
    )
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = White,
        modifier = modifier.padding(bottom = Dimensions.spacingLarge)
    )
}

@Composable
fun EmptyStateMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(Dimensions.paddingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = White
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
} 