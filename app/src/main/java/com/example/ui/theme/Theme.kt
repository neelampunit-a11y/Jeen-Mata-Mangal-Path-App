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
    primary = DevotionalGold,
    onPrimary = DevotionalCrimsonDark,
    primaryContainer = DevotionalCrimsonDark,
    onPrimaryContainer = DevotionalGoldLight,
    secondary = DevotionalAmber,
    onSecondary = Color.Black,
    secondaryContainer = DevotionalNightCard,
    onSecondaryContainer = DevotionalGold,
    background = DevotionalNightBg,
    onBackground = DevotionalNightText,
    surface = DevotionalNightSurface,
    onSurface = DevotionalNightText,
    surfaceVariant = DevotionalNightCard,
    onSurfaceVariant = DevotionalNightSecondaryText,
    outline = DevotionalAmber.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = DevotionalCrimson,
    onPrimary = Color.White,
    primaryContainer = DevotionalGoldLight,
    onPrimaryContainer = DevotionalCrimsonDark,
    secondary = DevotionalSaffron,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = DevotionalCrimsonDark,
    tertiary = DevotionalAmber,
    background = DevotionalParchment,
    onBackground = DevotionalTextDark,
    surface = DevotionalParchmentCard,
    onSurface = DevotionalTextDark,
    surfaceVariant = Color(0xFFF2EADC),
    onSurfaceVariant = DevotionalTextSecondary,
    outline = DevotionalBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
