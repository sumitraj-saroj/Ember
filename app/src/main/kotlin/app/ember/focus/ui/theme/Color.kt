package app.ember.focus.ui.theme

import androidx.compose.ui.graphics.Color

// Background gradient tones
val BackgroundTop = Color(0xFF0A0705)
val BackgroundBottom = Color(0xFF14100C)

// Surface colors
val SurfaceCard = Color(0xFF171310)
val SurfaceCardElevated = Color(0xFF1F1915)
val SurfaceBorder = Color(0xFF2B221B)
val TableWoodTop = Color(0xFF1C1510)
val TableWoodBottom = Color(0xFF120E0A)

// Typography & Text
val TextCream = Color(0xFFF2E6D0)
val TextMuted = Color(0xFFA39688) // WCAG AA compliant (>= 6:1 contrast on dark surfaces)
val TextMutedDark = Color(0xFF8C8176) // G1 FieldLabel color
val TextSubtle = Color(0xFF5E554C)
val TextDark = Color(0xFF1A120C)
val MutedCream = Color(0xFFB9AD9A) // G2 unit text color

// Primary Accent & Buttons
val AmberAccent = Color(0xFFE8A15A)
val AmberGlow = Color(0xFFFF9E44)
val AmberDark = Color(0xFFC47E38)

val PrimaryPillFill = Color(0xFFEBDCC3)
val PrimaryPillText = Color(0xFF1A120C)

val SecondaryPillFill = Color(0xFF1C1815)
val SecondaryPillText = Color(0xFFF2E6D0)
val SecondaryPillBorder = Color(0xFF2E2620)

// Slider & Switch & Chip tokens
val TrackInactive = Color(0xFF2A241F)
val SwitchOffTrack = Color(0xFF1C1815)
val SwitchOffBorder = Color(0xFF5A5047)
val SwitchOffThumb = Color(0xFF8C8176)
val ChipUnselectedFill = Color(0xFF1C1815)
val ChipUnselectedBorder = Color(0xFF2A2520)

// Candle & Wax (C1 - C6 warm only, no cool grays)
val WaxIvory = Color(0xFFF5EFE6)
val WaxShadow = Color(0xFFD4C8B5)
val WaxDarkCool = Color(0xFF8C847C) // For broken candle
val WaxEdge = Color(0xFFBFAE8E)
val WaxMid = Color(0xFFEFE3CB)
val WaxHighlight = Color(0xFFFBF4E4)
val WaxAmbientOcclusion = Color(0xFF8F7F63)
val WaxHaloAmber = Color(0xFFFF9A3C)
val BrassDish = Color(0xFFC2974F)
val BrassDishShadow = Color(0xFF6E5224)
val WickColor = Color(0xFF1E1712)

// States
val AshGray = Color(0xFF6E675F)
val AshGraySubtle = Color(0xFF38332E)
val SuccessGreen = Color(0xFF81C784)

// G4: Distinct Dark-Friendly Category Palette (8 distinct swatches)
val CategoryPalette = listOf(
    Color(0xFFE8A15A), // Work (amber)
    Color(0xFFE07A6A), // Study (coral)
    Color(0xFF78A9C7), // Coding (muted sky)
    Color(0xFFB38FD6), // Writing (soft violet)
    Color(0xFF8DBE8A), // Reading (sage)
    Color(0xFFD9C46A), // Straw
    Color(0xFF5FBFAE), // Teal
    Color(0xFFD98BB0)  // Rose
)

val CategoryPaletteHex = listOf(
    "#E8A15A",
    "#E07A6A",
    "#78A9C7",
    "#B38FD6",
    "#8DBE8A",
    "#D9C46A",
    "#5FBFAE",
    "#D98BB0"
)

// Legacy compatibility
val CategoryColors = CategoryPalette
