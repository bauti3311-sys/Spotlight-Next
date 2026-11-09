package dev.bautista.spotlight.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** Obsidian and Atelier are separate materials; neutral surfaces never inherit a brand fill. */
internal object PremiumMaterial {
    fun palette(base:ColorScheme,dark:Boolean):ColorScheme {
        val neutral=base.copy(
            background=if(dark)Color(0xFF11151B) else Color(0xFFE8EBEF),
            surface=if(dark)Color(0xFF20252D) else Color(0xFFFAF9F6),
            onSurface=if(dark)Color(0xFFF0F2F6) else Color(0xFF202B3A),
            onSurfaceVariant=if(dark)Color(0xFFB7C1D0) else Color(0xFF536173),
            surfaceVariant=if(dark)Color(0xFF2B333F) else Color(0xFFE8EDF2),
            surfaceContainerLowest=if(dark)Color(0xFF161B22) else Color(0xFFE9EDF1),
            surfaceContainerLow=if(dark)Color(0xFF1B212A) else Color(0xFFF0F2F4),
            surfaceContainer=if(dark)Color(0xFF232A34) else Color(0xFFFAF9F6),
            surfaceContainerHigh=if(dark)Color(0xFF303947) else Color(0xFFFFFFFF),
            surfaceContainerHighest=if(dark)Color(0xFF364151) else Color(0xFFE4EAF1),
            outline=if(dark)Color(0xFF66768D) else Color(0xFF8190A4),
            outlineVariant=if(dark)Color(0xFF3B4657) else Color(0xFFCDD5DF))
        return accent(neutral,base.primary)
    }
    fun accent(base:ColorScheme,raw:Color):ColorScheme {
        val end=if(base.surface.luminance()<.5f)Color.White else Color.Black
        val surfaces=listOf(base.surface,base.surfaceContainerLow,base.surfaceContainerHigh)
        val accessible=(0..20).asSequence().map{lerp(raw.copy(alpha=1f),end,it/20f)}
            .firstOrNull{color->surfaces.all{InspectorAccent.contrast(color,it)>=4.5f}} ?: end
        return InspectorAccent.palette(base,accessible)
    }
}

/** Review-only opt-in. No saved preference, no launcher action, no change to shipped defaults.
 * The owner reviews real Compose comparisons before adopting material composition changes. */
internal val LocalPremiumReview = staticCompositionLocalOf { false }
internal val LocalInspectorReviewBudget = staticCompositionLocalOf { Float.POSITIVE_INFINITY }
