package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GalaxyColorScheme = darkColorScheme(
    primary = GalaxyCyan,
    onPrimary = GalaxyVoid,
    primaryContainer = GalaxySurfaceVariant,
    onPrimaryContainer = GalaxyCyan,
    secondary = GalaxyElectricBlue,
    onSecondary = GalaxyVoid,
    secondaryContainer = GalaxySurfaceVariant,
    onSecondaryContainer = GalaxyTextPrimary,
    tertiary = GalaxyViolet,
    onTertiary = GalaxyVoid,
    background = GalaxyVoid,
    onBackground = GalaxyTextPrimary,
    surface = GalaxyDeep,
    onSurface = GalaxyTextPrimary,
    surfaceVariant = GalaxySurface,
    onSurfaceVariant = GalaxyTextSecondary,
    outline = GalaxyCardBorder,
    error = GalaxyError,
    onError = GalaxyVoid
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GalaxyColorScheme,
        typography = Typography,
        content = content
    )
}
