package com.kiranahisab.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KiranaLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = AmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = OnAmberContainer,
    background = SurfaceCanvas,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantTonal,
    onSurfaceVariant = TextMuted,
    outline = OutlineSubtle,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun KiranaHisabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KiranaLightColorScheme,
        content = content
    )
}
