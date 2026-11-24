package com.example.mediastoreexampleapp.core.designsystem

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
    primary = Black700,
    secondary = Purple400,
    background = Black900,
    tertiary = Color(0xFF242230),
    outline = Black500,

    onPrimary = Color.White,
    onSecondary = Gray500,
    onTertiary = Color(0xFFA39BFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color.White,
    secondary = Purple400,
    background = Color(0xFFF4F6FA),
    tertiary = Purple200,
    outline = Gray500,

    onPrimary = Color.Black,
    onSecondary = Color.Gray,
    onTertiary = Color(0xFF6855DE)
)

@Composable
fun MediaStoreExampleAppTheme(
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