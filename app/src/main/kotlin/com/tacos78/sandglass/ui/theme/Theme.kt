package com.tacos78.sandglass.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = SandGold,
    onPrimary = OnSand,
    primaryContainer = SandContainer,
    onPrimaryContainer = Charcoal,
    secondary = Terracotta,
    onSecondary = OnSand,
    secondaryContainer = SandContainer,
    onSecondaryContainer = Charcoal,
    tertiary = TealDepth,
    onTertiary = OnSand,
    background = Cream,
    onBackground = Charcoal,
    surface = Cream,
    onSurface = Charcoal,
    surfaceContainer = CreamContainer,
    surfaceContainerHigh = Color(0xFFEAD9C4),
    outline = Color(0xFFB08968),
)

private val DarkColorScheme = darkColorScheme(
    primary = SandGoldLight,
    onPrimary = Charcoal,
    primaryContainer = Color(0xFF7C4A12),
    onPrimaryContainer = SandContainer,
    secondary = Color(0xFFF0AB91),
    onSecondary = Charcoal,
    tertiary = Color(0xFF5EEAD4),
    onTertiary = Charcoal,
    background = Charcoal,
    onBackground = OnDark,
    surface = Charcoal,
    onSurface = OnDark,
    surfaceContainer = CharcoalContainer,
    surfaceContainerHigh = Color(0xFF3A2E27),
    outline = Color(0xFFC4A484),
)

@Composable
fun SandglassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SandglassTypography,
        content = content,
    )
}
