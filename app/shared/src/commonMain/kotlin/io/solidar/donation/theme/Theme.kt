package io.solidar.donation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme =
    lightColorScheme(
        primary = Emerald500,
        onPrimary = OnAccentWhite,
        primaryContainer = Emerald100,
        onPrimaryContainer = Emerald900,
        secondary = Teal500,
        onSecondary = OnAccentWhite,
        secondaryContainer = Teal100,
        onSecondaryContainer = Teal900,
        background = LightBackground,
        onBackground = TextPrimaryLight,
        surface = LightSurface,
        onSurface = TextPrimaryLight,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = TextSecondaryLight,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Emerald500,
        onPrimary = OnAccentWhite,
        primaryContainer = Emerald900,
        onPrimaryContainer = Emerald100,
        secondary = Teal500,
        onSecondary = OnAccentWhite,
        secondaryContainer = Teal900,
        onSecondaryContainer = Teal100,
        background = DarkBackground,
        onBackground = TextPrimaryDark,
        surface = DarkSurface,
        onSurface = TextPrimaryDark,
        surfaceVariant = DarkSurfaceVariant,
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
