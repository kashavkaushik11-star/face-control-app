package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF9CF0FF),
    secondary = ElectricViolet,
    onSecondary = Color(0xFF330066),
    secondaryContainer = Color(0xFF4C1D95),
    onSecondaryContainer = Color(0xFFDDD6FE),
    tertiary = SolarAmber,
    onTertiary = Color(0xFF422006),
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = TechDarkBackground,
    onBackground = TextPrimary,
    surface = TechSurface,
    onSurface = TextPrimary,
    surfaceVariant = TechSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TechSurfaceBorder,
    error = CoralRed,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme( // Keep tech-dark aesthetic primary
    primary = CyberCyan,
    onPrimary = Color(0xFF00363D),
    secondary = ElectricViolet,
    tertiary = SolarAmber,
    background = TechDarkBackground,
    surface = TechSurface
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep signature consistent tech palette for pointer visibility
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
