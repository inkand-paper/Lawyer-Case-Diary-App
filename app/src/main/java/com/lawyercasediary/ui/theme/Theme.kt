package com.lawyercasediary.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LegalBlue20,
    onPrimary = Color(0xFF0A1128),
    primaryContainer = LegalBlue40,
    onPrimaryContainer = Color(0xFFDCE3F5),
    secondary = LegalSilver,
    onSecondary = Color(0xFF1D2D44),
    secondaryContainer = Color(0xFF2A3B57),
    onSecondaryContainer = LegalSilver,
    tertiary = LegalGold,
    onTertiary = Color(0xFF2B2100),
    tertiaryContainer = Color(0xFF3D2F00),
    onTertiaryContainer = Color(0xFFF5DD8A),
    background = LegalDarkBg,
    onBackground = Color(0xFFE3E6ED),
    surface = LegalDarkSurface,
    onSurface = Color(0xFFE3E6ED),
    surfaceVariant = Color(0xFF283656),
    onSurfaceVariant = LegalSilver,
    outline = Color(0xFF6C7A94),
    outlineVariant = Color(0xFF3A4A6B),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF4E0002)
)

private val LightColorScheme = lightColorScheme(
    primary = LegalBlue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE3F5),
    onPrimaryContainer = LegalBlue80,
    secondary = LegalBlue20,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8ECF7),
    onSecondaryContainer = LegalBlue80,
    tertiary = LegalGold,
    onTertiary = Color(0xFF2B2100),
    tertiaryContainer = Color(0xFFFCEFC7),
    onTertiaryContainer = Color(0xFF5C4700),
    background = BackgroundLight,
    onBackground = Color(0xFF1A1C1E),
    surface = SurfaceLight,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE7ECF3),
    onSurfaceVariant = Color(0xFF44474A),
    outline = LegalSilver,
    outlineVariant = Color(0xFFDDE2E8),
    error = StatusUrgent,
    onError = Color.White
)

private val LegalShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun LawyerCaseDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Was defaulting to true — on any Android 12+ device (most phones and
    // emulators today) that silently discards the whole Legal Blue/Gold
    // palette below and replaces it with colors extracted from the user's
    // wallpaper instead. That's the single biggest reason the app was
    // reading as generic/basic: none of the actual palette design was
    // visible on most test devices.
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
            window.statusBarColor = colorScheme.primary.toArgb()
            // NOTE: this used `!darkTheme` here, which is wrong whenever the
            // status bar's actual background (colorScheme.primary) is a dark
            // color in light mode too — which it is here (LegalBlue40/20 are
            // both dark navy, in both themes). That produced dark status bar
            // icons on a dark navy background in light mode: hard to read.
            // Status bar icon color should follow the background's actual
            // luminance, not the theme flag.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                colorScheme.primary.luminance() > 0.5f
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = LegalShapes,
        content = content
    )
}
