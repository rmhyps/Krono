package com.krono.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KronoDarkColorScheme = darkColorScheme(
    primary = KronoPrimary,
    onPrimary = KronoOnPrimary,
    primaryContainer = KronoPrimaryContainer,
    onPrimaryContainer = KronoOnPrimaryContainer,
    secondary = KronoSecondary,
    tertiary = KronoTertiary,
    background = KronoBackground,
    surface = KronoSurface,
    surfaceVariant = KronoSurfaceContainer,
    onSurface = KronoOnSurface,
    onSurfaceVariant = KronoOnSurfaceVariant,
    outline = KronoOutline,
    outlineVariant = KronoOutlineVariant,
    error = KronoError,
    errorContainer = KronoErrorContainer,
    onErrorContainer = KronoOnErrorContainer
)

@Composable
fun KronoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KronoDarkColorScheme,
        typography = KronoTypography,
        content = content
    )
}
