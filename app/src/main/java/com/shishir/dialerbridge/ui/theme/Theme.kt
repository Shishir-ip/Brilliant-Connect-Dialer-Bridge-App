package com.shishir.dialerbridge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Paper,
    onPrimary = Ink,
    primaryContainer = DarkCardHigh,
    onPrimaryContainer = Paper,
    background = DarkBackground,
    onBackground = Paper,
    surface = DarkBackground,
    onSurface = Paper,
    surfaceContainerLowest = Color(0xFF0A0A0A),
    surfaceContainerLow = DarkCard,
    surfaceContainer = DarkCard,
    surfaceContainerHigh = DarkCardHigh,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkSecondaryText,
    outlineVariant = DarkSeparator,
    error = DestructiveDark,
    onError = Ink
)

private val LightScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    primaryContainer = Color(0xFFE8E8ED),
    onPrimaryContainer = Ink,
    background = LightBackground,
    onBackground = Ink,
    surface = LightCard,
    onSurface = Ink,
    surfaceContainerLowest = Paper,
    surfaceContainerLow = LightCard,
    surfaceContainer = LightCard,
    surfaceContainerHigh = Color(0xFFE8E8ED),
    onSurfaceVariant = LightSecondaryText,
    outline = LightSecondaryText,
    outlineVariant = LightSeparator,
    error = DestructiveLight,
    onError = Paper
)

@Composable
fun BridgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = BridgeTypography,
        content = content
    )
}
