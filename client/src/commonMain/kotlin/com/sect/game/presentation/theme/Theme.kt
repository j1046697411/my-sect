package com.sect.game.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SectColors.JadeGreen,
    onPrimary = SectColors.SilkWhite,
    primaryContainer = SectColors.JadeGreenLight,
    onPrimaryContainer = SectColors.JadeGreenDark,
    secondary = SectColors.Gold,
    onSecondary = SectColors.SilkWhite,
    secondaryContainer = SectColors.GoldLight,
    onSecondaryContainer = SectColors.GoldDark,
    tertiary = SectColors.NascentSoul,
    onTertiary = SectColors.SilkWhite,
    background = SectColors.SpiritBackground,
    onBackground = SectColors.TextPrimary,
    surface = SectColors.SilkWhite,
    onSurface = SectColors.TextPrimary,
    surfaceVariant = SectColors.SpiritBackground,
    onSurfaceVariant = SectColors.TextSecondary,
    error = SectColors.Error,
    onError = SectColors.SilkWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = SectColors.JadeGreenLight,
    onPrimary = SectColors.JadeGreenDark,
    primaryContainer = SectColors.JadeGreen,
    onPrimaryContainer = SectColors.SilkWhite,
    secondary = SectColors.GoldLight,
    onSecondary = SectColors.GoldDark,
    secondaryContainer = SectColors.Gold,
    onSecondaryContainer = SectColors.SilkWhite,
    tertiary = SectColors.NascentSoul,
    onTertiary = SectColors.SpiritBackgroundDark,
    background = SectColors.SpiritBackgroundDark,
    onBackground = SectColors.TextPrimaryDark,
    surface = SectColors.SilkWhiteDark,
    onSurface = SectColors.TextPrimaryDark,
    surfaceVariant = SectColors.SpiritBackgroundDark,
    onSurfaceVariant = SectColors.TextSecondaryDark,
    error = SectColors.Error,
    onError = SectColors.SpiritBackgroundDark
)

@Composable
fun SectTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SectTypography,
        content = content
    )
}
