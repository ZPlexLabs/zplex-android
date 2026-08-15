package zechs.zplex.common.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006A6A),
    onPrimary = Color.White,
    secondary = Color(0xFF4D6261),
    tertiary = Color(0xFF50617A),
    background = Color(0xFFF7FAF9),
    surface = Color(0xFFF7FAF9)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4CDAD8),
    onPrimary = Color(0xFF003737),
    secondary = Color(0xFFB1CCCA),
    tertiary = Color(0xFFB5C8E8),
    background = Color(0xFF101414),
    surface = Color(0xFF101414)
)

@Composable
fun ZplexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ZplexTypography,
        shapes = ZplexShapes,
        content = content
    )
}