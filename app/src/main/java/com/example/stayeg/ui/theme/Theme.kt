package com.example.stayeg.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StayEgEditorialColorScheme = lightColorScheme(
    primary = TealPrimary,              // #076653 Rich Emerald Teal
    onPrimary = PureWhite,
    primaryContainer = SoftSagePill,    // Soft Sage Container
    onPrimaryContainer = TealDeep,      // #06231D
    secondary = SoftGoldAccent,         // Soft Warm Gold
    onSecondary = PureWhite,
    secondaryContainer = SoftGoldPill,  // Soft Champagne Gold
    onSecondaryContainer = GoldDark,
    tertiary = MintSoft,                // #E2FBCE
    onTertiary = TealDeep,
    background = SoftGalleryBg,         // #FAF8F5 Warm Gallery Ivory
    onBackground = TextPrimary,
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = SoftCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = WhisperBorder
)

@Composable
fun StayEgTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = StayEgEditorialColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
