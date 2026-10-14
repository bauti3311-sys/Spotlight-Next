package dev.bautista.spotlight.search

import android.app.Application
import dev.bautista.spotlight.data.ContextStore
import dev.bautista.spotlight.data.Preferences
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], application=Application::class)
class AutomaticContextRegressionTest {
    private fun scenario(block: suspend (ContextStore) -> Unit) = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val preferences = Preferences(context)
        preferences.update(AppConfig(captureContext=true))
        preferences.config.first()
        val scope = CoroutineScope(SupervisorJob()+Dispatchers.IO)
        try { block(ContextStore(context,scope,preferences)) } finally { scope.cancel() }
    }

    @Test fun repeatedCapturePreservesIdentityAndExpiry() = scenario { store ->
        store.offerAutomatic("same clipboard")
        val original = store.active.value!!
        repeat(4) { store.offerAutomatic("same clipboard") }
        assertEquals(original,store.active.value)
        store.expire(original.created+600000)
        assertNull(store.active.value)
        store.offerAutomatic("same clipboard")
        assertNull("Focus regain must not resurrect expired context",store.active.value)
    }

    @Test fun dismissalIsRespectedUntilClipboardActuallyChanges() = scenario { store ->
        store.offerAutomatic("first")
        store.clearActive()
        store.offerAutomatic("first")
        assertNull(store.active.value)
        store.offerAutomatic("second")
        assertEquals("second",store.active.value!!.text)
        store.offerAutomatic("first")
        assertEquals("first",store.active.value!!.text)
    }

    @Test fun automaticCaptureDoesNotReplaceExplicitContext() = scenario { store ->
        val explicit = store.setText("selected by user")
        store.offerAutomatic("unrelated clipboard")
        assertEquals(explicit,store.active.value)
        store.clearActive()
        store.setText("unrelated clipboard")
        assertEquals("unrelated clipboard",store.active.value!!.text)
    }
}
