package dev.bautista.spotlight.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Presentation selector only; the existing FocusSearchState and Android contracts stay shared. */
internal val LocalFocusProductFinish=staticCompositionLocalOf{false}
internal data class FocusTextRoles(val query:TextStyle,val contextual:TextStyle,val title:TextStyle,
 val result:TextStyle,val subtitle:TextStyle,val metadata:TextStyle,val shortcut:TextStyle,val section:TextStyle)
internal val FocusLegacyRoles=FocusTextRoles(FocusSearchType.query,FocusSearchType.contextual,FocusSearchType.title,
 FocusSearchType.result,FocusSearchType.subtitle,FocusSearchType.metadata,FocusSearchType.shortcut,FocusSearchType.section)
internal object FocusProductType {
 private fun role(size:Int,line:Int,weight:FontWeight=FontWeight.Normal,tracking:Float=0f)=TextStyle(
  fontFamily=Geist,fontSize=size.sp,lineHeight=line.sp,fontWeight=weight,letterSpacing=tracking.sp)
 val roles=FocusTextRoles(role(22,30,tracking=-.25f),role(20,28,tracking=-.2f),role(16,22,FontWeight.SemiBold),
  role(16,22,FontWeight.SemiBold,tracking=-.1f),role(13,19),role(12,17),role(11,15),role(12,17,FontWeight.SemiBold,tracking=.15f))
 fun palette(base:ColorScheme):ColorScheme {
  val dark=base.surface.luminance()<.5f
  return base.copy(surface=if(dark)Color(0xFF26282C) else Color(0xFFFAF8F3),
   surfaceContainerLow=if(dark)Color(0xFF1B1D20) else Color(0xFFF0ECE4),
   surfaceContainerHigh=if(dark)Color(0xFF303237) else Color(0xFFFFFDF8),
   onSurface=if(dark)Color(0xFFF1F0ED) else Color(0xFF252629),
   onSurfaceVariant=if(dark)Color(0xFFB9BCC1) else Color(0xFF5B5C60),
   outlineVariant=if(dark)Color(0xFF4B4E54) else Color(0xFFCFC9BE))
 }
}
