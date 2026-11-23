package com.example.stayeg.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// STAYEG PREMIUM SAGE, TEAL & SOFT GOLD PALETTE
// Aligned with Attitude Creative (Image 1) + WeDo Luxury Editorial UI (Image 2)
// ============================================================================

// Core Palette Tokens
val CreamLight = Color(0xFFFFFDEE)        // Soft Gold Cream (#FFFDEE)
val MintSoft = Color(0xFFE2FBCE)          // Soft Pastel Sage (#E2FBCE)
val LimeVibrant = Color(0xFFE3EF26)       // Luminous Gold-Lime (#E3EF26)
val TealPrimary = Color(0xFF076653)       // Rich Emerald Teal (#076653)
val TealDark = Color(0xFF0C342C)          // Deep Forest Teal (#0C342C)
val TealDeep = Color(0xFF06231D)          // Ultra-Dark Forest (#06231D)

// Soft Gold & Sage Editorial Surfaces (Matching "WeDo" Reference Image 2)
val SoftGalleryBg = Color(0xFFFAF8F5)     // Warm Ivory Gallery Background
val SoftCardSurface = Color(0xFFF3F1EC)   // Warm Alabaster / Sage-Tinted Card Fill
val SoftSagePill = Color(0xFFE8F3EE)      // Soft Sage Badge Fill
val SoftGoldPill = Color(0xFFFAF0DF)      // Soft Champagne Gold Badge Fill
val SoftGoldAccent = Color(0xFFC88A32)    // Refined Warm Gold Text/Icon Accent
val SoftCoralPill = Color(0xFFF9EAE6)     // Soft Blush Accent (from WeDo Screen 5)
val WhisperBorder = Color(0xFFE8E6E0)     // Subtle Warm Hairline Border

// Compatibility Aliases used across screens
val PitchBlack = TealDeep                 // #06231D Deepest Forest Charcoal
val TrueBlack = Color(0xFF06231D)
val PersianBlue = TealPrimary             // #076653 Rich Emerald Teal
val ElectricLime = MintSoft               // #E2FBCE Soft Sage-Mint Highlight
val MintCream = SoftSagePill              // #E8F3EE Soft Sage Surface
val PureWhite = Color(0xFFFFFFFF)

// Signature Gradients
val StayEgGradient1 = Brush.verticalGradient(
    colors = listOf(CreamLight, LimeVibrant)
)

val StayEgGradient2 = Brush.verticalGradient(
    colors = listOf(MintSoft, LimeVibrant, TealPrimary)
)

val StayEgGradient3 = Brush.verticalGradient(
    colors = listOf(LimeVibrant, TealPrimary, TealDark)
)

val StayEgGradient4 = Brush.linearGradient(
    colors = listOf(TealPrimary, TealDark, TealDeep),
    start = Offset(0f, 0f),
    end = Offset(900f, 500f)
)

val StayEgBlueLimeGradient = StayEgGradient4
val StayEgDarkHeroGradient = StayEgGradient4
val StayEgMintWhiteGradient = Brush.verticalGradient(
    colors = listOf(SoftSagePill, PureWhite)
)

// Semantic Aliases
val ForestEmerald = TealPrimary
val ForestDeep = TealDark
val ForestUltraDark = TealDeep

val PrimaryBrand = TealPrimary
val PrimaryDark = TealDark
val PrimaryLight = SoftSagePill
val SecondaryAccent = SoftGoldAccent

val BluePrimary = TealPrimary
val BlueDeep = TealDark
val BlueLight = SoftSagePill
val Blue100 = MintSoft

val BackgroundLight = SoftGalleryBg
val SurfaceWhite = PureWhite
val SageSurface = SoftCardSurface
val SageSoft = SoftSagePill
val MintBadgeBg = SoftSagePill

val TextPrimary = Color(0xFF14241F)       // Deep Forest Ink
val TextSecondary = Color(0xFF63706B)     // Muted Sage-Slate
val TextMuted = Color(0xFF929E99)         // Soft Pewter
val BorderLight = WhisperBorder

val VerifiedGreen = TealPrimary
val VerifiedGreenBg = SoftSagePill
val SuccessGreen = TealPrimary
val SuccessGreenLight = SoftSagePill

val GoldAccent = SoftGoldAccent
val GoldLight = SoftGoldPill
val GoldDark = Color(0xFF8C5A14)

val WarningAmber = SoftGoldAccent
val WarningAmberBg = SoftGoldPill
val WarningAmberLight = SoftGoldPill
val ErrorRed = Color(0xFFC93B2B)
val ErrorRedBg = Color(0xFFFCEAE8)
val ErrorRedLight = Color(0xFFFCEAE8)
val InfoBlue = TealPrimary
val InfoBlueBg = SoftSagePill

val ChipSelectedBg = TealDark
val ChipUnselectedBg = SoftCardSurface
