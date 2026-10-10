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
    primary = Color(0xFFA6C8FF),
    onPrimary = Color(0xFF003062),
    primaryContainer = WordPrimaryDark,
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFBCC7DB),
    onSecondary = Color(0xFF273141),
    secondaryContainer = Color(0xFF3D4758),
    onSecondaryContainer = Color(0xFFD8E3F8),
    tertiary = Color(0xFF90CCFF),
    background = Color(0xFF111418),
    surface = Color(0xFF191C20),
    surfaceVariant = Color(0xFF25292E),
    outline = Color(0xFF4B5563),
    outlineVariant = Color(0xFF374151)
)

private val LightColorScheme = lightColorScheme(
    primary = WordPrimary,
    onPrimary = Color.White,
    primaryContainer = WordPrimaryContainer,
    onPrimaryContainer = WordOnPrimaryContainer,
    secondary = WordSecondary,
    onSecondary = Color.White,
    secondaryContainer = WordSecondaryContainer,
    onSecondaryContainer = WordOnSecondaryContainer,
    tertiary = WordTertiary,
    onTertiary = Color.White,
    tertiaryContainer = WordTertiaryContainer,
    onTertiaryContainer = WordOnTertiaryContainer,
    background = WordDeskCanvasLight,
    surface = Color.White,
    surfaceVariant = Color(0xFFF3F4F6),
    outline = WordOutline,
    outlineVariant = WordOutlineVariant
)

@Composable
fun SmartWordTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep signature Microsoft Word blue branding consistent by default
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
