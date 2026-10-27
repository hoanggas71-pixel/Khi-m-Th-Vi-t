package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpaLightColorScheme = lightColorScheme(
    primary = SpaRosewood,
    onPrimary = Color.White,
    primaryContainer = SpaChampagneLight,
    onPrimaryContainer = SpaRosewoodDark,
    secondary = SpaChampagne,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5EBE0),
    onSecondaryContainer = SpaTextPrimary,
    tertiary = SpaSageGreen,
    onTertiary = Color.White,
    tertiaryContainer = SpaSageLight,
    onTertiaryContainer = Color(0xFF283629),
    error = SpaDanger,
    onError = Color.White,
    errorContainer = SpaDangerLight,
    onErrorContainer = Color(0xFF5C1F1F),
    background = SpaCreamBg,
    onBackground = SpaTextPrimary,
    surface = SpaSurface,
    onSurface = SpaTextPrimary,
    surfaceVariant = Color(0xFFF1EAE4),
    onSurfaceVariant = SpaTextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SpaLightColorScheme,
        typography = Typography,
        content = content
    )
}
