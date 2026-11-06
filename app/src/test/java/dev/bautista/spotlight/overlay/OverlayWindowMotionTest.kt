package dev.bautista.spotlight.overlay

import android.app.Activity
import android.widget.EditText
import android.widget.FrameLayout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = android.app.Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class OverlayWindowMotionTest {
    @Test fun openingRemainsVisibleAndAcceptsInputBeforeAnimationFinishes() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            val activity = controller.get()
            val host = FrameLayout(activity)
            val editor = EditText(activity)
            host.addView(editor)
            OverlayWindowMotion.prepare(host, true)
            activity.setContentView(host)
            OverlayWindowMotion.open(host, true)
            assertEquals(140L,host.animate().duration)
            assertTrue(host.alpha > .9f)
            assertTrue(editor.requestFocus())
            editor.setText("spot")
            assertEquals("spot", editor.text.toString())
            assertTrue(editor.hasFocus())
            shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(200))
            assertEquals(1f, host.alpha, .001f)
            assertEquals(0f, host.translationY, .001f)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun reducedMotionIsImmediatelyVisibleAndClosesWithoutAnAnimationCallback() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            val host = FrameLayout(activity.get())
            activity.get().setContentView(host)
            OverlayWindowMotion.prepare(host, false)
            OverlayWindowMotion.open(host, false)
            assertEquals(1f, host.alpha, 0f)
            assertEquals(0f, host.translationY, 0f)
            var closed = false
            OverlayWindowMotion.close(host, false) { closed = true }
            assertTrue(closed)
        } finally { activity.pause().stop().destroy() }
    }
    @Test fun actualCloseDurationIs100msAndKeepsTheHostLayout() {
        val controller=Robolectric.buildActivity(Activity::class.java).setup()
        try {
            val host=FrameLayout(controller.get())
            controller.get().setContentView(host)
            host.layoutParams=FrameLayout.LayoutParams(320,200)
            var finished=0
            OverlayWindowMotion.close(host,true) { finished++ }
            assertEquals(100L,host.animate().duration)
            shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(200))
            assertEquals(1,finished)
            assertEquals(320,host.layoutParams.width);assertEquals(200,host.layoutParams.height)
        } finally {controller.pause().stop().destroy()}
    }

}
