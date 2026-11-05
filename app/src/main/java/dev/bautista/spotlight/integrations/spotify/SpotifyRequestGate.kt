package dev.bautista.spotlight.integrations.spotify

import java.util.concurrent.atomic.AtomicBoolean

/** Reject concurrent confirmations; never queue or replay a playback request. */
internal class SpotifyRequestGate {
    private val busy = AtomicBoolean(false)
    fun enter(): Boolean = busy.compareAndSet(false, true)
    fun leave() { busy.set(false) }
}
