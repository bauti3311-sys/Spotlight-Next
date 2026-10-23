package dev.bautista.spotlight.ui

import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class InspectorAccentTest {
    private fun app(pkg: String, title: String = "Otra app") = Result("$pkg/Main", title, "", "Aplicaciones", 0,
        Action.Launch(Intent().setComponent(ComponentName(pkg, "Main"))))

    @Test fun identityIsExactAndNeverGuessedFromResultTitleOrQuery() {
        assertEquals(Color(0xFF65D993), InspectorAccent.brand("com.spotify.music"))
        assertEquals(Color(0xFFF2958B), InspectorAccent.brand("com.google.android.gm"))
        assertNull(InspectorAccent.brand("com.fake.spotify"))
        assertNull(InspectorAccent.brand("com.whatsapp.w4b"))
        val result = app("com.example.reader", "Spotify")
        assertEquals("com.example.reader", InspectorAccent.destination(result, null, null, null, false))
        assertNull(InspectorAccent.brand(InspectorAccent.destination(result, null, null, null, false)))
    }

    @Test fun formDestinationWinsAndUnrelatedTasksNeverInheritSelectedApplication() {
        val selected = app("com.spotify.music")
        assertEquals("com.whatsapp", InspectorAccent.destination(selected, Action.ComposeMessage("whatsapp"), null, null, true))
        assertEquals("com.android.chrome", InspectorAccent.destination(selected, null,
            Action.ComposeSearch("web", "com.android.chrome"), null, true))
        assertNull(InspectorAccent.destination(selected, null, null, null, true))
        assertNull(InspectorAccent.destination(selected, Action.ComposeMessage("email"), null, null, true))
        assertNull(InspectorAccent.destination(selected, null, null, Action.SystemPanel("interpret", "Spotify"), true))
    }

    @Test fun darkAndLightAccentsKeepReadableControlsWithoutChangingErrorOrSurface() {
        val packages = listOf("com.spotify.music", "com.whatsapp", "com.google.android.gm", "com.android.chrome", "com.google.android.youtube")
        val bases = listOf(darkColorScheme(surface = Color(0xFF191C24)), lightColorScheme())
        for (base in bases) for (pkg in packages) {
            val palette = InspectorAccent.palette(base, InspectorAccent.brand(pkg)!!)
            assertTrue("$pkg text contrast", InspectorAccent.contrast(palette.primary, palette.surface) >= 4.5f)
            assertTrue("$pkg button contrast", InspectorAccent.contrast(palette.primary, palette.onPrimary) >= 4.5f)
            assertEquals(base.error, palette.error)
            assertEquals(base.surface, palette.surface)
            assertEquals(base.onSurface, palette.onSurface)
        }
    }

    @Test fun unknownAppsUseBoundedIconColorAndDoNotMutateSharedDrawable() {
        val drawable = ColorDrawable(android.graphics.Color.rgb(30, 150, 110))
        drawable.setBounds(7, 9, 80, 90)
        val bounds = android.graphics.Rect(drawable.bounds)
        assertEquals(Color(0xFF1E966E), InspectorAccent.iconColor(drawable))
        assertEquals(bounds, drawable.bounds)
        assertNull(InspectorAccent.iconColor(ColorDrawable(android.graphics.Color.TRANSPARENT)))
        assertNull(InspectorAccent.iconColor(ColorDrawable(android.graphics.Color.GRAY)))
        assertNull(InspectorAccent.iconColor(null))
    }
}
