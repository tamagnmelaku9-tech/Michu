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

private val DarkColorScheme =
  darkColorScheme(
    primary = MichuPrimary,
    secondary = MichuSecondary,
    tertiary = MichuTertiary,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = DarkBg,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MichuPrimary,
    secondary = MichuSecondary,
    tertiary = MichuTertiary,
    background = LightBg,
    surface = LightSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = LightText,
    onBackground = LightText,
    onSurface = LightText
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep the custom brand look consistent
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
