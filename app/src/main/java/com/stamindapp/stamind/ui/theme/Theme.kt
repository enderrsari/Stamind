package com.stamindapp.stamind.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StamindLightColorScheme = lightColorScheme(
    primary = StamindColors.Green500,
    error = StamindColors.Red500,
    background = StamindColors.BackgroundColor,
    surface = StamindColors.BackgroundColor,
    onSurface = StamindColors.TextColor,
    outline = StamindColors.Green200,
)

private val StamindDarkColorScheme = darkColorScheme(
    primary = StamindColors.Green500,
    error = StamindColors.Red500,
    background = StamindColors.Green900,
    onSurface = StamindColors.White,
    outline = StamindColors.Green200,
)

@Composable
fun StamindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        StamindDarkColorScheme
    } else {
        StamindLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
