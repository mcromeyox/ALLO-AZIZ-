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
    primary = AzizOrangeDarkTheme,
    onPrimary = Color.White,
    primaryContainer = AzizOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = AzizAmberSecondary,
    onSecondary = Color.Black,
    tertiary = AzizMint,
    onTertiary = Color.White,
    background = AzizBgDark,
    surface = AzizSurfaceDark,
    surfaceVariant = AzizSurfaceVariantDark,
    onBackground = AzizTextPrimaryDark,
    onSurface = AzizTextPrimaryDark,
    onSurfaceVariant = AzizTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = AzizOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = AzizOrangeContainer,
    onPrimaryContainer = AzizOrangeDark,
    secondary = AzizAmberSecondary,
    onSecondary = Color.Black,
    tertiary = AzizMint,
    onTertiary = Color.White,
    background = AzizBgLight,
    surface = AzizSurfaceLight,
    surfaceVariant = AzizSurfaceVariant,
    onBackground = AzizTextPrimary,
    onSurface = AzizTextPrimary,
    onSurfaceVariant = AzizTextSecondary
)

@Composable
fun AlloAzizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded orange aesthetic consistent
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
