package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val CleanLightColorScheme =
  lightColorScheme(
    primary = ActiveIcon,
    onPrimary = Color.White,
    primaryContainer = PrimaryAccent,
    onPrimaryContainer = PrimaryAccentText,
    secondary = ActiveIcon,
    background = BgLight,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextTitle,
    surfaceVariant = CardSecondary,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    outlineVariant = CardSecondaryBorder
  )

@Composable
fun MyApplicationTheme(
  // Always light theme for clean minimalism
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = CleanLightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
