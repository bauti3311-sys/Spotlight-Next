package dev.bautista.spotlight.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import dev.bautista.spotlight.R
import dev.bautista.spotlight.data.Config
import androidx.compose.runtime.staticCompositionLocalOf

val Inter=FontFamily(Font(R.font.inter))
private val BaseTypography=Typography()
private val SpotlightTypography=Typography(
    displayLarge=BaseTypography.displayLarge.copy(fontFamily=Inter),
    displayMedium=BaseTypography.displayMedium.copy(fontFamily=Inter),
    displaySmall=BaseTypography.displaySmall.copy(fontFamily=Inter),
    headlineLarge=BaseTypography.headlineLarge.copy(fontFamily=Inter),
    headlineMedium=BaseTypography.headlineMedium.copy(fontFamily=Inter),
    headlineSmall=BaseTypography.headlineSmall.copy(fontFamily=Inter),
    titleLarge=BaseTypography.titleLarge.copy(fontFamily=Inter),
    titleMedium=BaseTypography.titleMedium.copy(fontFamily=Inter),
    titleSmall=BaseTypography.titleSmall.copy(fontFamily=Inter),
    bodyLarge=BaseTypography.bodyLarge.copy(fontFamily=Inter),
    bodyMedium=BaseTypography.bodyMedium.copy(fontFamily=Inter),
    bodySmall=BaseTypography.bodySmall.copy(fontFamily=Inter),
    labelLarge=BaseTypography.labelLarge.copy(fontFamily=Inter),
    labelMedium=BaseTypography.labelMedium.copy(fontFamily=Inter),
    labelSmall=BaseTypography.labelSmall.copy(fontFamily=Inter),
)

val Accent = Color(0xFF0A84FF)
private val Dark =
    darkColorScheme(
        primary = Accent,
        background = Color(0xFF17191F),
        surface = Color(0xFF242730),
        onSurface = Color(0xFFF5F5F5),
        surfaceVariant = Color(0xFF20232B),
    )
private val Light =
    lightColorScheme(
        primary = Color(0xFF007AFF),
        background = Color(0xFFF2F2F2),
        surface = Color(0xFFFAFBFD),
        onSurface = Color(0xFF222222),
        surfaceVariant = Color(0xFFF0F2F7),
        onSurfaceVariant = Color(0xFF666666),
    )

val LocalAppearance = staticCompositionLocalOf { Config() }

@Composable
fun SpotlightTheme(theme: String = "auto", config: Config = Config(theme=theme), content: @Composable () -> Unit) {
    val dark = theme == "dark" || (theme == "auto" && isSystemInDarkTheme())
    val accent = Color(when(config.accent) { "graphite" -> if(dark)0xFFADB5C4 else 0xFF566172; "purple" -> if(dark)0xFFB8A0FF else 0xFF7250CC; "rose" -> if(dark)0xFFFF8FAF else 0xFFBF3A62; "green" -> if(dark)0xFF66D6AE else 0xFF137453; "orange" -> if(dark)0xFFFFB878 else 0xFFAD4D0B; else -> if(dark)0xFF7CB7FF else 0xFF0869D1 })
    val base=if(dark) Dark else Light
    val palette = when(config.preset){
        "hybrid"->base.copy(background=if(dark)Color(0xFF151618) else Color(0xFFEDEEF0),surface=if(dark)Color(0xFF202124) else Color(0xFFFAFAF8),surfaceVariant=if(dark)Color(0xFF292B2F) else Color(0xFFF0F1EF),onSurface=if(dark)Color(0xFFF2F3F5) else Color(0xFF202226),onSurfaceVariant=if(dark)Color(0xFFBEC1C8) else Color(0xFF59616B))
        "raycast"->base.copy(background=if(dark)Color(0xFF171719) else Color(0xFFF0EFF3),surface=if(dark)Color(0xFF242426) else Color(0xFFFCFBFD),surfaceVariant=if(dark)Color(0xFF2C2C30) else Color(0xFFE9E7EE),onSurfaceVariant=if(dark)Color(0xFFB0ADB8) else Color(0xFF64606D))
        "graphite"->base.copy(background=if(dark)Color(0xFF1D2023) else Color(0xFFF2F3F4),surface=if(dark)Color(0xFF262A2D) else Color.White,surfaceVariant=if(dark)Color(0xFF303438) else Color(0xFFE9ECEF))
        "ios"->base.copy(background=if(dark)Color(0xFF15171C) else Color(0xFFF2F2F7),surface=if(dark)Color(0xFF23252B) else Color.White,surfaceVariant=if(dark)Color(0xFF2D3037) else Color(0xFFF2F2F7))
        else->base
    }.let{p->p.copy(primary=accent,onPrimary=if(accent.luminance()>.179f)Color(0xFF12151B) else Color.White,primaryContainer=lerp(p.surface,accent,if(dark).18f else .10f),onPrimaryContainer=p.onSurface,secondary=accent,onSecondary=if(accent.luminance()>.179f)Color(0xFF12151B) else Color.White,secondaryContainer=lerp(p.surface,accent,if(dark).12f else .08f),onSecondaryContainer=p.onSurface,tertiary=accent,onTertiary=p.background,tertiaryContainer=lerp(p.surface,accent,.08f),onTertiaryContainer=p.onSurface,outline=p.onSurface.copy(alpha=.24f),outlineVariant=p.onSurface.copy(alpha=.10f),surfaceTint=accent,surfaceContainerLowest=p.background,surfaceContainerLow=lerp(p.surface,p.background,.35f),surfaceContainer=p.surface,surfaceContainerHigh=lerp(p.surface,p.surfaceVariant,.5f),surfaceContainerHighest=p.surfaceVariant)}
    MaterialTheme(colorScheme = palette, typography=SpotlightTypography) {
        val motion=rememberMotionAllowed(config.animations)
        CompositionLocalProvider(LocalMotion provides motion,LocalAppearance provides config, LocalContentColor provides MaterialTheme.colorScheme.onSurface, content=content)
    }
}
