package dev.bautista.spotlight.ui

import androidx.compose.material3.*
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bautista.spotlight.data.Config

/** Production design system. Opt-in presentation preserves the legacy interface until physical acceptance. */
val LocalFocusUi=staticCompositionLocalOf{false}
object FocusDesign {
 val touch=48.dp
 val gap=12.dp
 val inset=20.dp
 val radius=22.dp
 fun family(c:Config):FontFamily=when(c.focusTypeface){"system"->FontFamily.Default;"mono"->FontFamily.Monospace;"inter"->Inter;else->Geist}
 fun typography(c:Config):Typography {
  val family=family(c);val scale=GlassTokens.textScale(c)
  fun role(size:Int,line:Int,weight:FontWeight=FontWeight.Normal)=TextStyle(fontFamily=family,fontSize=size.sp*scale,lineHeight=line.sp*scale,fontWeight=weight)
  return Typography(displayLarge=role(28,34,FontWeight.SemiBold),displayMedium=role(26,32,FontWeight.SemiBold),displaySmall=role(24,30,FontWeight.SemiBold),
   headlineLarge=role(24,30,FontWeight.SemiBold),headlineMedium=role(22,28,FontWeight.SemiBold),headlineSmall=role(20,26,FontWeight.SemiBold),
   titleLarge=role(20,28,FontWeight.SemiBold),titleMedium=role(17,23,FontWeight.SemiBold),titleSmall=role(15,21,FontWeight.SemiBold),
   bodyLarge=role(15,22),bodyMedium=role(14,20),bodySmall=role(13,19),labelLarge=role(13,19,FontWeight.SemiBold),labelMedium=role(12,17),labelSmall=role(12,17))
 }
 fun palette(base:ColorScheme,dark:Boolean,c:Config):ColorScheme {
  val tone=when(c.surfaceTone){"warm"->Color(0xFFFFCA8C);"cool"->Color(0xFF87BFFF);else->Color.Transparent}
  val surface=if(dark)Color(0xFF26282C) else Color(0xFFFAF8F3)
  val material=base.copy(background=if(dark)Color(0xFF111315) else Color(0xFFE9E6E0),
   surface=if(tone==Color.Transparent)surface else lerp(surface,tone,.035f),
   surfaceVariant=if(dark)Color(0xFF303237) else Color(0xFFE8E3DB),
   surfaceContainerLow=if(dark)Color(0xFF1B1D20) else Color(0xFFF0ECE4),surfaceContainerHigh=if(dark)Color(0xFF303237) else Color(0xFFFFFDF8),
   onSurface=if(dark)Color(0xFFF1F0ED) else Color(0xFF252629),onSurfaceVariant=if(dark)Color(0xFFB9BCC1) else Color(0xFF5B5C60),
   outlineVariant=if(dark)Color(0xFF4B4E54) else Color(0xFFCFC9BE))
  return PremiumMaterial.accent(material,base.primary)
 }
}
