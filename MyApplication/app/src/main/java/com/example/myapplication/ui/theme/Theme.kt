package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    secondary = SoftCyan,
    onSecondary = Color.Black,
    tertiary = VoltageYellow,
    onTertiary = Color.Black,
    background = DeepBlack,
    surface = SurfaceDark,
    onBackground = Color.White,
    onSurface = Color.White,
    error = DangerRed,
    onError = Color.White,
    surfaceVariant = SteelBlue,
    onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    secondary = SoftCyan,
    onSecondary = Color.White,
    tertiary = VoltageYellow,
    onTertiary = Color.Black,
    background = SoftBg,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = DangerRed,
    onError = Color.White,
    surfaceVariant = Color(0xFFE1E2E1),
    onSurfaceVariant = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
