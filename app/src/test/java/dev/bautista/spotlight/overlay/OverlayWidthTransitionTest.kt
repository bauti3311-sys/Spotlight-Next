package dev.bautista.spotlight.overlay

import android.animation.ValueAnimator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = android.app.Application::class)
class OverlayWidthTransitionTest {
    @Test fun replacementStartsAtCurrentWidthAndRejectsStaleFrames() {
        val animators = mutableListOf<ValueAnimator>()
        val transition = OverlayWidthTransition { from, to ->
            ValueAnimator.ofInt(from, to).also { animators.add(it) }
        }
        val widths = mutableListOf<Int>()
        transition.move(400, 800, true, 190) { widths.add(it) }
        animators[0].setCurrentFraction(.5f)
        val midpoint = widths.last()
        assertTrue(midpoint in 401..799)
        transition.move(midpoint, 500, true, 190) { widths.add(it) }
        val count = widths.size
        animators[0].setCurrentFraction(1f)
        assertEquals("Cancelled frames must not overwrite new geometry", count, widths.size)
        animators[1].end()
        assertEquals(500, widths.last())
    }

    @Test fun geometryRefreshAndReducedMotionApplyImmediately() {
        val animators = mutableListOf<ValueAnimator>()
        val transition = OverlayWidthTransition { from, to ->
            ValueAnimator.ofInt(from, to).also { animators.add(it) }
        }
        val widths = mutableListOf<Int>()
        transition.move(400, 800, true, 190) { widths.add(it) }
        transition.move(400, 360, false, 190) { widths.add(it) }
        assertEquals(360, widths.last())
        animators[0].setCurrentFraction(1f)
        assertEquals(360, widths.last())
        assertEquals(1, animators.size)
    }

    @Test fun closingCancelsOutstandingUpdates() {
        lateinit var animator: ValueAnimator
        val transition = OverlayWidthTransition { from, to -> ValueAnimator.ofInt(from, to).also { animator = it } }
        val widths = mutableListOf<Int>()
        transition.move(400, 800, true, 190) { widths.add(it) }
        transition.cancel()
        val count = widths.size
        animator.setCurrentFraction(1f)
        assertEquals(count, widths.size)
    }
}
