package dev.bautista.spotlight.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator

/** One native window-width transition. Old frames cannot mutate a replacement window. */
internal class OverlayWidthTransition(
    private val createAnimator: (Int, Int) -> ValueAnimator = { from, to -> ValueAnimator.ofInt(from, to) },
) {
    private var active: ValueAnimator? = null
    private var target: Int? = null

    fun move(from: Int, to: Int, animated: Boolean, duration: Long, apply: (Int) -> Unit) {
        if (animated && target == to && active?.isRunning == true) return
        cancel()
        if (!animated || from == to) {
            apply(to)
            return
        }
        val animator = createAnimator(from, to)
        active = animator
        target = to
        animator.duration = duration
        animator.addUpdateListener {
            if (active === animator) apply(it.animatedValue as Int)
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (active === animator) { active = null; target = null }
            }
        })
        animator.start()
    }

    fun cancel() {
        val previous = active
        active = null
        target = null
        previous?.cancel()
    }
}
