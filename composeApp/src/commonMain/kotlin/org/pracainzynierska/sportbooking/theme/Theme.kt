package org.pracainzynierska.sportbooking.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Kolor przewodni: Racing Green (Głęboka, angielska zieleń)
val RacingGreen = Color(0xFF004225)
// Kolor akcentu: Jaśniejsza zieleń
val RacingGreenLight = Color(0xFF2E6B48)
// Tło aplikacji
val AppBackground = Color(0xFFF9F9F9)
// Kolor błędu
val ErrorRed = Color(0xFFB00020)

// Definicja motywu
val AppColorScheme = lightColorScheme(
    primary = RacingGreen,
    onPrimary = Color.White,
    secondary = RacingGreenLight,
    onSecondary = Color.White,
    background = AppBackground,
    surface = Color.White,
    error = ErrorRed
)