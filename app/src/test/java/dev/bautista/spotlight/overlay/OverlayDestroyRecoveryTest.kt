package dev.bautista.spotlight.overlay

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.lifecycle.ViewModel
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.OverlaySnapshotCodec
import dev.bautista.spotlight.integrations.spotify.SpotifyItemType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class OverlayDestroyRecoveryTest {
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private class Sentinel:ViewModel() { var cleared=false;override fun onCleared(){cleared=true} }

    @Test fun invalidChildStateCannotCrashDestroyOrOverwriteTheLastGoodRecovery()=runBlocking {
        val good=mapOf("query" to listOf<Any?>("conservar borrador"))
        app.contexts.saveConversation("Conversación conservada","Respuesta local")
        app.contexts.saveOverlaySnapshot(good)
        val controller=Robolectric.buildService(OverlayService::class.java).create()
        val service=controller.get();val sentinel=Sentinel()
        service.viewModelStore.put("cleanup",sentinel)
        val registry=SaveableStateRegistry(null,OverlaySnapshotCodec::canSave)
        registry.registerProvider("unsupported-child"){mutableStateOf(SpotifyItemType.TRACK)}
        ReflectionHelpers.setField(service,"recoveryRegistry",registry)
        controller.destroy()
        assertTrue(sentinel.cleared)
        assertEquals(good,app.contexts.overlaySnapshot())
        assertTrue(app.contexts.conversations.value.any{it.title=="Conversación conservada"})
        val reopened=Robolectric.buildService(OverlayService::class.java).create()
        reopened.destroy()
        assertEquals(good,app.contexts.overlaySnapshot())
    }

    @Test fun throwingChildCannotPreventCleanupOrWritePrivateStateToLogs() {
        val controller=Robolectric.buildService(OverlayService::class.java).create()
        val service=controller.get();val sentinel=Sentinel()
        service.viewModelStore.put("cleanup",sentinel)
        val registry=SaveableStateRegistry(null,OverlaySnapshotCodec::canSave)
        registry.registerProvider("throwing-child"){error("PRIVATE_DRAFT_SENTINEL")}
        ReflectionHelpers.setField(service,"recoveryRegistry",registry)
        controller.destroy()
        assertTrue(sentinel.cleared)
        val logs=ShadowLog.getLogsForTag("SpotlightOverlay")
        assertTrue(logs.any{it.msg.contains("keeping the previous checkpoint")})
        assertTrue(logs.none{it.msg.contains("PRIVATE_DRAFT_SENTINEL") || it.throwable!=null})
    }
}
