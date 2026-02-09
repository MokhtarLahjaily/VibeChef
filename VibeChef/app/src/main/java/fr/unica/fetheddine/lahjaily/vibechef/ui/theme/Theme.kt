package fr.unica.fetheddine.lahjaily.vibechef.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Orange80,
    secondary = OrangeGrey80,
    tertiary = Green80,
    background = DarkChocolate,
    surface = Color(0xFF3A2820),
    onPrimary = Color(0xFF4A2800),
    onSecondary = Color(0xFF3B2E1E),
    onTertiary = Color(0xFF1A3612),
    onBackground = Color(0xFFF5EDE6),
    onSurface = Color(0xFFF5EDE6),
    surfaceVariant = Color(0xFF524438),
    onSurfaceVariant = Color(0xFFD8C4B4),
    primaryContainer = Color(0xFF6E3D00),
    onPrimaryContainer = Orange80,
    error = Color(0xFFFFB4AB)
)

private val LightColorScheme = lightColorScheme(
    primary = Orange40,
    secondary = OrangeGrey40,
    tertiary = Green40,
    background = Cream,
    surface = WarmSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF2C1810),
    onSurface = Color(0xFF2C1810),
    surfaceVariant = Color(0xFFF2E0D0),
    onSurfaceVariant = Color(0xFF52443A),
    primaryContainer = Color(0xFFFFDCC5),
    onPrimaryContainer = Color(0xFF3A1A00),
    error = Color(0xFFBA1A1A)
)

@Composable
fun VibeChefTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Tint the system status bar
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
        typography = Typography,
        content = content
    )
}