package dev.bautista.spotlight.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** Stitch reference translated to native materials. No geometry, motion or preference changes.
 * Opaque inner layers protect legibility over other apps; no blur/rendering dependency. */
internal object PuenteMaterial {
 fun palette(base:ColorScheme,dark:Boolean):ColorScheme = if(dark)base.copy(
  background=Color(0xFF0B1016),onBackground=Color(0xFFE8EEF5),
  surface=Color(0xFF10161E),surfaceContainerLow=Color(0xFF0D131A),
  surfaceContainer=Color(0xFF161E27),surfaceContainerHigh=Color(0xFF202A35),
  surfaceContainerHighest=Color(0xFF293440),onSurface=Color(0xFFE8EEF5),
  onSurfaceVariant=Color(0xFFA6B3C2),outlineVariant=Color(0xFF34414F))
 else base.copy(background=Color(0xFFF1F4F7),onBackground=Color(0xFF182533),
  surface=Color(0xFFFAFCFE),surfaceContainerLow=Color(0xFFEEF2F6),
  surfaceContainer=Color(0xFFE8EEF3),surfaceContainerHigh=Color(0xFFE0E8EF),
  surfaceContainerHighest=Color(0xFFD6E0EA),onSurface=Color(0xFF182533),
  onSurfaceVariant=Color(0xFF506174),outlineVariant=Color(0xFFBDC9D5))
}
