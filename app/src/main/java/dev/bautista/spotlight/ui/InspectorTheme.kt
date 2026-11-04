package dev.bautista.spotlight.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result

/** Presentation only: destination identity, never query text, determines the accent. */
internal object InspectorAccent {
    fun brand(packageName: String?): Color? = when (packageName) {
        "com.spotify.music" -> Color(0xFF65D993)
        "com.whatsapp", "com.whatsapp.w4b" -> Color(0xFF59D3A5)
        "com.google.android.gm" -> Color(0xFFF2958B)
        "com.android.chrome" -> Color(0xFF8CB5FF)
        "com.google.android.youtube" -> Color(0xFFEE898D)
        else -> null
    }

    fun destination(item: Result?, message: Action.ComposeMessage?, search: Action.ComposeSearch?,
                    page: Action.SystemPanel?, taskActive: Boolean): String? = when {
        message != null -> when (message.channel) {
            "whatsapp" -> "com.whatsapp"
            "whatsapp_business" -> "com.whatsapp.w4b"
            "gmail" -> "com.google.android.gm"
            else -> null
        }
        search != null -> search.packageName.takeIf { it.isNotBlank() }
        page != null -> page.packageName.takeIf { page.page == "app" && it.isNotBlank() }
        taskActive -> null // An unrelated selection must not recolor a pinned AI/form/plan task.
        PreviewPresentation.isApplication(item) -> (item?.action as? Action.Launch)?.intent?.component?.packageName
        else -> null
    }

    fun result(item:Result?):Color? {
        val pkg=destination(item,null,null,null,false) ?: return null
        return brand(pkg) ?: iconColor(item?.icon)
    }

    /** A private, bounded 32px copy; never alter a Drawable shared with the result list. */
    fun iconColor(drawable: Drawable?): Color? {
        val copy = drawable?.constantState?.newDrawable()?.mutate() ?: return null
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        return try {
            copy.setBounds(0, 0, 32, 32)
            copy.draw(Canvas(bitmap))
            val counts = IntArray(24)
            val red = LongArray(24); val green = LongArray(24); val blue = LongArray(24)
            val hsv = FloatArray(3)
            for (y in 0 until 32 step 2) for (x in 0 until 32 step 2) {
                val pixel = bitmap.getPixel(x, y)
                android.graphics.Color.colorToHSV(pixel, hsv)
                if (android.graphics.Color.alpha(pixel) < 192 || hsv[1] < .28f || hsv[2] < .25f) continue
                val bin = (hsv[0] / 15f).toInt().coerceIn(0, 23)
                counts[bin]++
                red[bin] += android.graphics.Color.red(pixel)
                green[bin] += android.graphics.Color.green(pixel)
                blue[bin] += android.graphics.Color.blue(pixel)
            }
            val best = counts.indices.maxByOrNull { counts[it] } ?: return null
            val count = counts[best]
            if (count < 6) null else Color(android.graphics.Color.rgb(
                (red[best] / count).toInt(), (green[best] / count).toInt(), (blue[best] / count).toInt()))
        } catch (_: Exception) { null } finally { bitmap.recycle() }
    }

    fun contrast(a: Color, b: Color): Float {
        val l1 = a.luminance(); val l2 = b.luminance()
        return (maxOf(l1, l2) + .05f) / (minOf(l1, l2) + .05f)
    }

    fun palette(base: ColorScheme, raw: Color): ColorScheme {
        val end = if (base.surface.luminance() < .5f) Color.White else Color.Black
        val primary = (0..20).asSequence().map { lerp(raw.copy(alpha = 1f), end, it / 20f) }
            .firstOrNull { contrast(it, base.surface) >= 4.5f } ?: end
        val darkInk = Color(0xFF101318)
        val ink = if (contrast(primary, darkInk) > contrast(primary, Color.White)) darkInk else Color.White
        return base.copy(primary = primary, onPrimary = ink,
            primaryContainer = lerp(base.surface, primary, .13f), onPrimaryContainer = base.onSurface,
            secondary = primary, onSecondary = ink,
            secondaryContainer = lerp(base.surface, primary, .12f), onSecondaryContainer = base.onSurface)
    }
}

/** One scoped theme on the existing inspector. Search, ActionPanel, error colors and settings stay neutral. */
@Composable internal fun InspectorTheme(packageName: String?, icon: Drawable?, content: @Composable () -> Unit) {
    val base = MaterialTheme.colorScheme
    val accent = remember(packageName, icon, base.primary) {
        if (packageName == null) base.primary
        else InspectorAccent.brand(packageName) ?: InspectorAccent.iconColor(icon) ?: base.primary
    }
    val palette = remember(base, accent) { InspectorAccent.palette(base, accent) }
    MaterialTheme(colorScheme = palette) {
        CompositionLocalProvider(LocalContentColor provides palette.onSurface) {
            Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(
                palette.primary.copy(alpha = .025f), Color.Transparent)))
                .drawBehind {
                    val x=if(layoutDirection==androidx.compose.ui.unit.LayoutDirection.Rtl)size.width-1.dp.toPx() else 1.dp.toPx()
                    drawLine(palette.primary.copy(alpha=.50f),Offset(x,12.dp.toPx()),Offset(x,minOf(size.height,36.dp.toPx())),strokeWidth=1.5.dp.toPx())
                }) { content() }
        }
    }
}

/** A contextual glyph/control accent; never recolor the graphite surface or global Settings. */
@Composable internal fun rememberCommandAccent(item:Result?):Color? {
    val base=MaterialTheme.colorScheme
    val raw=remember(item?.id,item?.action,item?.icon){InspectorAccent.result(item)}
    return remember(base,raw){raw?.let{InspectorAccent.palette(base,it).primary}}
}
