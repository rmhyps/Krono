package com.krono.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// --- Dark Obsidian Palette ---
val DarkBackground = Color(0xFF0D0F12)
val DarkSurface = Color(0xFF0D0F12)
val DarkSurfaceLowest = Color(0xFF07080A)
val DarkSurfaceLow = Color(0xFF13151A)
val DarkSurfaceContainer = Color(0xFF181B22)
val DarkSurfaceHigh = Color(0xFF222630)
val DarkSurfaceHighest = Color(0xFF2D323E)
val DarkOnSurface = Color(0xFFF1F3F6)
val DarkOnSurfaceVariant = Color(0xFF8C93A0)
val DarkOutline = Color(0xFF2A2F3C)
val DarkOutlineVariant = Color(0xFF1B1E26)
val DarkPrimary = Color(0xFF6366F1)
val DarkOnPrimary = Color(0xFFFFFFFF)
val DarkPrimaryContainer = Color(0xFF282B4E)
val DarkOnPrimaryContainer = Color(0xFFC7D2FE)
val DarkSecondary = Color(0xFF60A5FA)
val DarkTertiary = Color(0xFF34D399)
val DarkWarning = Color(0xFFFBBF24)
val DarkError = Color(0xFFF87171)
val DarkErrorContainer = Color(0xFF450A0A)
val DarkOnErrorContainer = Color(0xFFFECACA)

// --- Light Minimalist Palette ---
val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceLowest = Color(0xFFFFFFFF)
val LightSurfaceLow = Color(0xFFF3F4F6)
val LightSurfaceContainer = Color(0xFFFFFFFF)
val LightSurfaceHigh = Color(0xFFF1F3F5)
val LightSurfaceHighest = Color(0xFFE5E7EB)
val LightOnSurface = Color(0xFF111827)
val LightOnSurfaceVariant = Color(0xFF6B7280)
val LightOutline = Color(0xFFE5E7EB)
val LightOutlineVariant = Color(0xFFF3F4F6)
val LightPrimary = Color(0xFF4F46E5)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFEEF2FF)
val LightOnPrimaryContainer = Color(0xFF312E81)
val LightSecondary = Color(0xFF2563EB)
val LightTertiary = Color(0xFF059669)
val LightWarning = Color(0xFFD97706)
val LightError = Color(0xFFDC2626)
val LightErrorContainer = Color(0xFFFEE2E2)
val LightOnErrorContainer = Color(0xFF991B1B)

data class KronoColors(
    val background: Color,
    val surface: Color,
    val surfaceLowest: Color,
    val surfaceLow: Color,
    val surfaceContainer: Color,
    val surfaceHigh: Color,
    val surfaceHighest: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val tertiary: Color,
    val warning: Color,
    val error: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val isDark: Boolean
)

val DarkKronoColors = KronoColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceLowest = DarkSurfaceLowest,
    surfaceLow = DarkSurfaceLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceHigh = DarkSurfaceHigh,
    surfaceHighest = DarkSurfaceHighest,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    warning = DarkWarning,
    error = DarkError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    isDark = true
)

val LightKronoColors = KronoColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceLowest = LightSurfaceLowest,
    surfaceLow = LightSurfaceLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceHigh = LightSurfaceHigh,
    surfaceHighest = LightSurfaceHighest,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    warning = LightWarning,
    error = LightError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    isDark = false
)

val LocalKronoColors = staticCompositionLocalOf { DarkKronoColors }

// Dynamic Composable Properties
val KronoBackground: Color @Composable get() = LocalKronoColors.current.background
val KronoSurface: Color @Composable get() = LocalKronoColors.current.surface
val KronoSurfaceLowest: Color @Composable get() = LocalKronoColors.current.surfaceLowest
val KronoSurfaceLow: Color @Composable get() = LocalKronoColors.current.surfaceLow
val KronoSurfaceContainer: Color @Composable get() = LocalKronoColors.current.surfaceContainer
val KronoSurfaceHigh: Color @Composable get() = LocalKronoColors.current.surfaceHigh
val KronoSurfaceHighest: Color @Composable get() = LocalKronoColors.current.surfaceHighest
val KronoOnSurface: Color @Composable get() = LocalKronoColors.current.onSurface
val KronoOnSurfaceVariant: Color @Composable get() = LocalKronoColors.current.onSurfaceVariant
val KronoOutline: Color @Composable get() = LocalKronoColors.current.outline
val KronoOutlineVariant: Color @Composable get() = LocalKronoColors.current.outlineVariant
val KronoPrimary: Color @Composable get() = LocalKronoColors.current.primary
val KronoOnPrimary: Color @Composable get() = LocalKronoColors.current.onPrimary
val KronoPrimaryContainer: Color @Composable get() = LocalKronoColors.current.primaryContainer
val KronoOnPrimaryContainer: Color @Composable get() = LocalKronoColors.current.onPrimaryContainer
val KronoSecondary: Color @Composable get() = LocalKronoColors.current.secondary
val KronoTertiary: Color @Composable get() = LocalKronoColors.current.tertiary
val KronoWarning: Color @Composable get() = LocalKronoColors.current.warning
val KronoError: Color @Composable get() = LocalKronoColors.current.error
val KronoErrorContainer: Color @Composable get() = LocalKronoColors.current.errorContainer
val KronoOnErrorContainer: Color @Composable get() = LocalKronoColors.current.onErrorContainer
