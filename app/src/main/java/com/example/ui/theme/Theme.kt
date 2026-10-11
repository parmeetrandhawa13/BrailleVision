package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrailleDarkColorScheme = darkColorScheme(
  primary = BrailleAmberPrimary,
  onPrimary = BrailleBlack,
  primaryContainer = BrailleAmberContainer,
  onPrimaryContainer = BrailleAmberOnContainer,
  secondary = BrailleCyanSecondary,
  onSecondary = BrailleBlack,
  secondaryContainer = BrailleCyanContainer,
  onSecondaryContainer = BrailleCyanBright,
  background = BrailleBlack,
  onBackground = BrailleTextHighContrast,
  surface = BrailleDarkSurface,
  onSurface = BrailleTextHighContrast,
  surfaceVariant = BrailleCardSurface,
  onSurfaceVariant = BrailleTextHighContrast,
  outline = BrailleBorder,
  error = BrailleErrorRed,
  onError = Color.White
)

private val BrailleLightColorScheme = lightColorScheme(
  primary = Color(0xFFE65100),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFE0B2),
  onPrimaryContainer = Color(0xFFE65100),
  secondary = Color(0xFF00695C),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFB2DFDB),
  onSecondaryContainer = Color(0xFF004D40),
  background = Color(0xFFFAFAFA),
  onBackground = Color(0xFF121212),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF121212),
  surfaceVariant = Color(0xFFEEEEEE),
  onSurfaceVariant = Color(0xFF212121),
  outline = Color(0xFF757575),
  error = Color(0xFFD32F2F),
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to high contrast dark theme for visually impaired users
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) BrailleDarkColorScheme else BrailleLightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

