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

private val DarkColorScheme =
  darkColorScheme(
    primary = RustOrange,
    secondary = SandYellow,
    tertiary = HazardGreen,
    background = WastelandBlack,
    surface = CarbonDark,
    onBackground = TextLight,
    onSurface = TextLight,
    onPrimary = TextLight,
    error = LaserRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RustOrange,
    secondary = PetrolBlue,
    tertiary = HazardGreen,
    background = DustGrey,
    surface = Color.White,
    onBackground = WastelandBlack,
    onSurface = WastelandBlack,
    onPrimary = Color.White,
    error = LaserRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for that extreme gaming vibe
  dynamicColor: Boolean = false, // Keep the custom hand-crafted theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
