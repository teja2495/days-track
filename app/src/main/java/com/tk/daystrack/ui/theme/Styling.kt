package com.tk.daystrack.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Centralized styling constants for the app
 * This file contains all the dimensions, shapes, and other UI-related constants
 */

// ===== DIMENSIONS =====

object Dimensions {
    // Padding and margins
    val paddingSmall = 8.dp
    val paddingMedium = 16.dp
    val paddingLarge = 24.dp
    val paddingExtraLarge = 32.dp
    
    // Spacing between elements
    val spacingSmall = 4.dp
    val spacingMedium = 8.dp
    val spacingLarge = 12.dp
    val spacingExtraLarge = 20.dp
    
    // Card and surface dimensions
    val cardCornerRadius = 16.dp
    val cardCornerRadiusSmall = 12.dp
    val cardCornerRadiusLarge = 24.dp
    val cardElevation = 4.dp
    val cardElevationPressed = 2.dp
    
    // Button dimensions
    val buttonCornerRadius = 50.dp
    val buttonPaddingHorizontal = 32.dp
    val buttonPaddingVertical = 12.dp
    
    // Icon sizes
    val iconSizeSmall = 20.dp
    val iconSizeMedium = 24.dp
    val iconSizeLarge = 26.dp
    val iconSizeExtraLarge = 30.dp
    
    // Text field dimensions
    val textFieldCornerRadius = 12.dp
    val textFieldBorderWidth = 1.dp
    
    // FAB dimensions
    val fabSize = 56.dp
    val fabIconSize = 24.dp
    
    // Header dimensions
    val headerHeight = 64.dp
    val headerPaddingTop = 12.dp
    val headerPaddingBottom = 22.dp
    
    // List item dimensions
    val listItemSpacing = 12.dp
    val listItemPadding = 16.dp
    
    // Bottom sheet dimensions
    val bottomSheetCornerRadius = 24.dp
    val bottomSheetPadding = 24.dp
    
    // Dialog dimensions
    val dialogCornerRadius = 24.dp
    val dialogPadding = 24.dp
    
    // Banner dimensions
    val bannerCornerRadius = 16.dp
    val bannerPadding = 12.dp
    val bannerElevation = 2.dp
}

// ===== SHAPES =====

object Shapes {
    val cardShape = RoundedCornerShape(Dimensions.cardCornerRadius)
    val cardShapeSmall = RoundedCornerShape(Dimensions.cardCornerRadiusSmall)
    val cardShapeLarge = RoundedCornerShape(Dimensions.cardCornerRadiusLarge)
    
    val buttonShape = RoundedCornerShape(Dimensions.buttonCornerRadius)
    val textFieldShape = RoundedCornerShape(Dimensions.textFieldCornerRadius)
    
    val bottomSheetShape = RoundedCornerShape(
        topStart = Dimensions.bottomSheetCornerRadius,
        topEnd = Dimensions.bottomSheetCornerRadius
    )
    
    val dialogShape = RoundedCornerShape(Dimensions.dialogCornerRadius)
    val bannerShape = RoundedCornerShape(Dimensions.bannerCornerRadius)
}

// ===== FONT SIZES =====

object FontSizes {
    // Title font sizes based on FontSize enum
    val titleSmall = 18.sp
    val titleMedium = 20.sp
    val titleLarge = 22.sp
    
    // Body font sizes based on FontSize enum
    val bodySmall = 14.sp
    val bodyMedium = 16.sp
    val bodyLarge = 18.sp
    
    // Card padding based on FontSize enum
    val cardPaddingSmall = 12.dp
    val cardPaddingMedium = 14.dp
    val cardPaddingLarge = 16.dp
}

// ===== ELEVATIONS =====

object Elevations {
    val cardDefault = Dimensions.cardElevation
    val cardPressed = Dimensions.cardElevationPressed
    val banner = Dimensions.bannerElevation
    val dialog = 8.dp
    val bottomSheet = 4.dp
}

// ===== ALPHA VALUES =====

object AlphaValues {
    val disabled = 0.5f
    val secondary = 0.7f
    val tertiary = 0.3f
    val border = 0.3f
    val shadow = 0.1f
} 