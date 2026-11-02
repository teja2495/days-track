package com.tk.daystrack.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            contentColor = ButtonContentColor,
            disabledContainerColor = ButtonDisabledColor,
            disabledContentColor = ButtonContentColor.copy(alpha = 0.7f)
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
        Text(text, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
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
        Text(text, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDeleteDialog: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = { Text(message, color = MaterialTheme.colorScheme.onSurface) },
        confirmButton = {
            if (isDeleteDialog) {
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier
                ) {
                    Text(confirmText, color = DeleteButtonColor)
                }
            } else {
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier
                ) {
                    Text(confirmText, color = ThemeTextColor, fontWeight = FontWeight.Bold)
                }
            }
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
    minLines: Int = 1,
    maxLines: Int = 1,
    focusRequester: androidx.compose.ui.focus.FocusRequester? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    isError: Boolean = false
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }
    
    // Update textFieldValue when value changes, but preserve selection if user is editing
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = textFieldValue.copy(text = value)
        }
    }
    
    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { 
            textFieldValue = it
            onValueChange(it.text) 
        },
        label = { Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
        modifier = modifier.let {
            if (focusRequester != null) it.focusRequester(focusRequester) else it
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
            focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else FocusedBorderColor,
            unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            focusedLabelColor = if (isError) MaterialTheme.colorScheme.error else FocusedLabelColor,
            unfocusedLabelColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            cursorColor = CursorColor,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = Shapes.textFieldShape,
        trailingIcon = trailingIcon,
        readOnly = readOnly,
        isError = isError
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
        color = MaterialTheme.colorScheme.onBackground,
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
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
