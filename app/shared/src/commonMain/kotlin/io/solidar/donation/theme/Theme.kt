package io.solidar.donation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
    lightColorScheme(
        primary = Emerald500,
        onPrimary = Color(0xFFFFFFFF),
        secondary = Teal500,
        onSecondary = Color(0xFFFFFFFF),
        background = LightBackground,
        onBackground = TextPrimaryLight,
        surface = LightSurface,
        onSurface = TextPrimaryLight,
        surfaceVariant = Color(0xFFF1F5F9),
        onSurfaceVariant = TextSecondaryLight,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Emerald500,
        onPrimary = Color(0xFFFFFFFF),
        secondary = Teal500,
        onSecondary = Color(0xFFFFFFFF),
        background = DarkBackground,
        onBackground = TextPrimaryDark,
        surface = DarkSurface,
        onSurface = TextPrimaryDark,
        surfaceVariant = Color(0xFF1E293B),
        onSurfaceVariant = TextSecondaryDark,
    )

@Composable
fun DonationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
