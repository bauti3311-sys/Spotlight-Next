package dev.bautista.spotlight.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*

/** Main-thread owner for manual document work; native resource ownership stays in DocumentText. */
internal class DocumentExtractionRequest {
    private var generation = 0L
    private var job: Job? = null
    var running by mutableStateOf(false)
        private set

    fun cancel() {
        generation++
        job?.cancel()
        job = null
        running = false
    }

    fun <T> start(
        scope: CoroutineScope,
        isCurrent: () -> Boolean,
        extract: suspend () -> T,
        publish: (T) -> Unit,
        failure: (Exception) -> Unit,
    ) {
        cancel()
        val request = generation
        running = true
        // Install the handle before running even when the caller uses an immediate dispatcher.
        val pending = scope.launch(start = CoroutineStart.LAZY) {
            try {
                val result = extract()
                ensureActive()
                if (request == generation && isCurrent()) publish(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ensureActive()
                if (request == generation && isCurrent()) failure(e)
            }
        }
        job = pending
        // Also runs if the scope was disposed before this lazy coroutine could enter its body.
        pending.invokeOnCompletion {
            if (request == generation) {
                job = null
                running = false
            }
        }
        pending.start()
    }
}
