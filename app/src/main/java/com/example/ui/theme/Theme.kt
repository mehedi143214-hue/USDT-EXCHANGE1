package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GreenAccent,
    secondary = GreenAccentDark,
    tertiary = Warning,
    background = NavyDark,
    surface = SurfaceDark,
    onPrimary = NavyDark,
    onSecondary = NavyDark,
    onTertiary = NavyDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = Danger
)

private val LightColorScheme = lightColorScheme(
    primary = GreenAccent,
    secondary = GreenAccentDark,
    tertiary = Warning,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFF020617),
    onBackground = Color(0xFF020617),
    onSurface = Color(0xFF0F172A),
    error = Danger
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
