package com.hippoddung.ribbit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    primaryContainer = Water40,
    secondary = GreenGrey80,
    secondaryContainer = Water40,
    tertiary = Water80,
    tertiaryContainer = Water40,
    surface = Water40,
    onSurface = Water40,
    background = Water40,
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = Water40,
//    onPrimary = Water80,
    primaryContainer = Green80, // FloatingActionButton 배경
//    onPrimaryContainer = Water80,
//    inversePrimary = Water80,
//    onSecondary = Water80,
//    secondaryContainer = Water80,
//    onSecondaryContainer = Water80,
    onTertiary = Green40,   // tertiary 내 글자
    tertiaryContainer = Water80,
    onTertiaryContainer = Water80,
    background = Color.White,
    onBackground = Green40,   // Card 내 글자
    surface = Color.White,    // 앱 배경
    onSurface = Green80,    // CardBottomBar ToggleIcons 변경 색상, InputTextField 글자 색
    surfaceVariant = Color.White,   // Card, TextField 배경
    inverseSurface = Water80,
    scrim = Water80,
)

@Composable
fun RibbitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // Dynamic color in this app is turned off for learning purposes
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}