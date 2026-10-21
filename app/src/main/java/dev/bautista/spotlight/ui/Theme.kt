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
        background = Color(0xFF18191A),
        surface = GlassTokens.graphite,
        onSurface = GlassTokens.darkInk,
        surfaceVariant = Color(0xFF28292A),
        onSurfaceVariant = GlassTokens.darkSecondary,
    )
private val Light =
    lightColorScheme(
        primary = Color(0xFF007AFF),
        background = Color(0xFFF2F2F2),
        surface = GlassTokens.paper,
        onSurfaceVariant = GlassTokens.lightSecondary,
        onSurface = GlassTokens.lightInk,
        surfaceVariant = Color(0xFFF0F2F7),
    )

val LocalAppearance = staticCompositionLocalOf { Config() }

@Composable
fun SpotlightTheme(theme: String = "auto", config: Config = Config(theme=theme), content: @Composable () -> Unit) {
    val dark = theme == "dark" || (theme == "auto" && isSystemInDarkTheme())
    val accent = Color(when(config.accent) { "graphite" -> if(dark)0xFFA1A1AA else 0xFF566172; "purple" -> if(dark)0xFFA855F7 else 0xFF7250CC; "rose" -> if(dark)0xFFF43F5E else 0xFFBF3A62; "green" -> if(dark)0xFF10B981 else 0xFF137453; "orange" -> if(dark)0xFFFFB878 else 0xFFAD4D0B; else -> if(dark)0xFF7275E8 else 0xFF5356BA })
    val base=if(dark) Dark else Light
    val palette = when(config.preset){
        "pure"->base.copy(background=if(dark)Color(0xFF111111) else Color.White,surface=if(dark)Color(0xFF171717) else Color.White,onSurface=if(dark)Color.White else Color(0xFF111111))
        "glass"->base.copy(background=if(dark)Color(0xFF101218) else Color(0xFFEFF2F7),surface=if(dark)Color(0xFF191C24) else GlassTokens.paper,surfaceVariant=if(dark)Color(0xFF222630) else Color(0xFFF0F0EE))
        "hybrid"->base.copy(background=if(dark)Color(0xFF151618) else Color(0xFFEDEEF0),surface=if(dark)Color(0xFF202124) else Color(0xFFFAFAF8),surfaceVariant=if(dark)Color(0xFF292B2F) else Color(0xFFF0F1EF),onSurface=if(dark)Color(0xFFF2F3F5) else Color(0xFF202226),onSurfaceVariant=if(dark)Color(0xFFBEC1C8) else Color(0xFF59616B))
        "raycast"->base.copy(background=if(dark)Color(0xFF191A1E) else Color(0xFFF0EFF3),surface=if(dark)Color(0xFF26272C) else Color(0xFFFCFBFD),surfaceVariant=if(dark)Color(0xFF222329) else Color(0xFFE9E7EE),onSurface=if(dark)Color(0xFFFAFAFC) else GlassTokens.lightInk,onSurfaceVariant=if(dark)Color(0xFFA0A0AB) else GlassTokens.lightSecondary)
        "graphite"->base.copy(background=if(dark)Color(0xFF1D2023) else Color(0xFFF2F3F4),surface=if(dark)Color(0xFF262A2D) else Color.White,surfaceVariant=if(dark)Color(0xFF303438) else Color(0xFFE9ECEF))
        "ios"->base.copy(background=if(dark)Color(0xFF15171C) else Color(0xFFF2F2F7),surface=if(dark)Color(0xFF23252B) else Color.White,surfaceVariant=if(dark)Color(0xFF2D3037) else Color(0xFFF2F2F7))
        else->base
    }.let{p->p.copy(primary=accent,onPrimary=if(accent.luminance()>.179f)Color(0xFF12151B) else Color.White,primaryContainer=lerp(p.surface,accent,if(dark).18f else .10f),onPrimaryContainer=p.onSurface,secondary=accent,onSecondary=if(accent.luminance()>.179f)Color(0xFF12151B) else Color.White,secondaryContainer=lerp(p.surface,accent,if(dark).12f else .08f),onSecondaryContainer=p.onSurface,tertiary=accent,onTertiary=p.background,tertiaryContainer=lerp(p.surface,accent,.08f),onTertiaryContainer=p.onSurface,outline=p.onSurface.copy(alpha=.24f),outlineVariant=p.onSurface.copy(alpha=.10f),surfaceTint=accent,surfaceContainerLowest=p.background,surfaceContainerLow=lerp(p.surface,p.background,.35f),surfaceContainer=p.surface,surfaceContainerHigh=lerp(p.surface,p.surfaceVariant,.5f),surfaceContainerHighest=p.surfaceVariant)}
    val font=when(config.typeface){"system"->FontFamily.Default;"mono"->FontFamily.Monospace;else->Inter}
    val typography=SpotlightTypography.copy(displayLarge=SpotlightTypography.displayLarge.copy(fontFamily=font,fontSize=SpotlightTypography.displayLarge.fontSize*config.textScale.coerceIn(.9f,1.3f)),
displayMedium=SpotlightTypography.displayMedium.copy(fontFamily=font,fontSize=SpotlightTypography.displayMedium.fontSize*config.textScale.coerceIn(.9f,1.3f)),
displaySmall=SpotlightTypography.displaySmall.copy(fontFamily=font,fontSize=SpotlightTypography.displaySmall.fontSize*config.textScale.coerceIn(.9f,1.3f)),
headlineLarge=SpotlightTypography.headlineLarge.copy(fontFamily=font,fontSize=SpotlightTypography.headlineLarge.fontSize*config.textScale.coerceIn(.9f,1.3f)),
headlineMedium=SpotlightTypography.headlineMedium.copy(fontFamily=font,fontSize=SpotlightTypography.headlineMedium.fontSize*config.textScale.coerceIn(.9f,1.3f)),
headlineSmall=SpotlightTypography.headlineSmall.copy(fontFamily=font,fontSize=SpotlightTypography.headlineSmall.fontSize*config.textScale.coerceIn(.9f,1.3f)),
titleLarge=SpotlightTypography.titleLarge.copy(fontFamily=font,fontSize=SpotlightTypography.titleLarge.fontSize*config.textScale.coerceIn(.9f,1.3f)),
titleMedium=SpotlightTypography.titleMedium.copy(fontFamily=font,fontSize=SpotlightTypography.titleMedium.fontSize*config.textScale.coerceIn(.9f,1.3f)),
titleSmall=SpotlightTypography.titleSmall.copy(fontFamily=font,fontSize=SpotlightTypography.titleSmall.fontSize*config.textScale.coerceIn(.9f,1.3f)),
bodyLarge=SpotlightTypography.bodyLarge.copy(fontFamily=font,fontSize=SpotlightTypography.bodyLarge.fontSize*config.textScale.coerceIn(.9f,1.3f)),
bodyMedium=SpotlightTypography.bodyMedium.copy(fontFamily=font,fontSize=SpotlightTypography.bodyMedium.fontSize*config.textScale.coerceIn(.9f,1.3f)),
bodySmall=SpotlightTypography.bodySmall.copy(fontFamily=font,fontSize=SpotlightTypography.bodySmall.fontSize*config.textScale.coerceIn(.9f,1.3f)),
labelLarge=SpotlightTypography.labelLarge.copy(fontFamily=font,fontSize=SpotlightTypography.labelLarge.fontSize*config.textScale.coerceIn(.9f,1.3f)),
labelMedium=SpotlightTypography.labelMedium.copy(fontFamily=font,fontSize=SpotlightTypography.labelMedium.fontSize*config.textScale.coerceIn(.9f,1.3f)),
labelSmall=SpotlightTypography.labelSmall.copy(fontFamily=font,fontSize=SpotlightTypography.labelSmall.fontSize*config.textScale.coerceIn(.9f,1.3f)))
    val tone=when(config.surfaceTone){"warm"->Color(0xFFFFCA8C);"cool"->Color(0xFF87BFFF);else->palette.surface}
    val toned=palette.copy(surface=lerp(palette.surface,tone,if(config.surfaceTone=="neutral")0f else .035f))
    MaterialTheme(colorScheme = toned, typography=typography) {
        val motion=rememberMotionAllowed(config.animations)
        CompositionLocalProvider(LocalMotion provides motion,LocalAppearance provides config, LocalContentColor provides MaterialTheme.colorScheme.onSurface, content=content)
    }
}
