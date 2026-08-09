package org.pacien.tincapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = HeaderPrimary,
    onPrimary = TextTitle,
    primaryContainer = HeaderPrimaryDark,
    secondary = TextAccent,
    background = BackgroundPrimary,
    surface = BackgroundPrimary,
    error = TextError,
    onBackground = TextSecondary,
    onSurface = TextSecondary
)

@Composable
fun TincTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // For now, we only support light theme as per the original XML styles
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
