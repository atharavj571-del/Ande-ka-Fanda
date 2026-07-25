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

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = SlateDarkBg,
    primaryContainer = SecondaryViolet,
    onPrimaryContainer = TextPrimaryDark,
    secondary = SecondaryViolet,
    onSecondary = TextPrimaryDark,
    tertiary = TertiaryAmber,
    onTertiary = SlateDarkBg,
    background = SlateDarkBg,
    onBackground = TextPrimaryDark,
    surface = CardDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardDarkBorder,
    onSurfaceVariant = TextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryCyan,
    onPrimary = SlateLightBg,
    primaryContainer = SecondaryViolet,
    onPrimaryContainer = TextPrimaryLight,
    secondary = SecondaryViolet,
    onSecondary = TextPrimaryLight,
    tertiary = TertiaryAmber,
    onTertiary = SlateLightBg,
    background = SlateLightBg,
    onBackground = TextPrimaryLight,
    surface = CardLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = TextSecondaryLight
)

@Composable
fun SyllabusTheme(
    darkTheme: Boolean = true, // Default to sleek obsidian dark theme for AI Intelligence feel
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
