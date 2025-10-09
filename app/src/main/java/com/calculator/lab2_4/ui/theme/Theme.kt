package com.calculator.lab2_4.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary   = Green80,
    secondary = TealGrey80,
    tertiary  = Lime80
)

private val LightColorScheme = lightColorScheme(
    primary   = Green40,
    secondary = TealGrey40,
    tertiary  = Lime40
)

@Composable
fun Lab24Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
