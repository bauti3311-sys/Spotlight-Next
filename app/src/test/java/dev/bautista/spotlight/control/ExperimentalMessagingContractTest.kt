package dev.bautista.spotlight.control

import android.app.Application
import dev.bautista.spotlight.experimental.whatsapp.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class ExperimentalMessagingContractTest {
    private class FixtureTransport : LinkedDeviceTransport {
        var sent = 0; var loggedOut = 0
        override suspend fun submit(draft: WhatsAppDraft): WhatsAppReceipt { sent++; return WhatsAppReceipt(WhatsAppMessageState.ACCEPTED,"fixture-receipt") }
        override suspend fun logout(): Boolean { loggedOut++; return true }
    }
    private val context get() = RuntimeEnvironment.getApplication()
    private fun draft(text: String = "Fixture", id: String = java.util.UUID.randomUUID().toString()) = WhatsAppDraft(text,recipientE164="+5491112345678",requestId=id)
    @Test fun disabledProviderNeverTouchesTransportEvenWithAnApproval() = runBlocking {
        val transport = FixtureTransport(); val gate = HumanReviewGate { 1000 }
        val provider = LinkedDeviceMessagingProvider(transport,gate,SendFence(context))
        val draft = draft(); assertFalse(provider.available(draft))
        val approval = gate.confirmFromUi(draft,provider.id)
        assertTrue(runCatching { provider.submit(draft,approval) }.isFailure)
        assertEquals(0,transport.sent); assertEquals(0,transport.loggedOut)
    }
    @Test fun reviewBindsExactDraftAndPersistentFenceRejectsDuplicateDispatch() = runBlocking {
        val transport = FixtureTransport(); val gate = HumanReviewGate { 1000 }; val fence = SendFence(context)
        val provider = LinkedDeviceMessagingProvider(transport,gate,fence,consented=true)
        val draft = draft(); val approval = gate.confirmFromUi(draft,provider.id)
        assertEquals(WhatsAppMessageState.ACCEPTED,provider.submit(draft,approval).state)
        val restored = LinkedDeviceMessagingProvider(transport,gate,SendFence(context),consented=true)
        assertTrue(runCatching { restored.submit(draft,gate.confirmFromUi(draft,restored.id)) }.isFailure)
        assertEquals(1,transport.sent)
    }
    @Test fun editedTextOrCancelledReviewNeverDispatchesAndDisconnectRevokes() = runBlocking {
        val transport = FixtureTransport(); val gate = HumanReviewGate { 1000 }
        val provider = LinkedDeviceMessagingProvider(transport,gate,SendFence(context),consented=true)
        val original = draft(); val approval = gate.confirmFromUi(original,provider.id)
        assertTrue(runCatching { provider.submit(draft("Edited",original.requestId),approval) }.isFailure)
        val pending = gate.confirmFromUi(original,provider.id); gate.cancelAll()
        assertTrue(runCatching { provider.submit(original,pending) }.isFailure)
        assertTrue(provider.disconnect()); assertFalse(provider.available(original))
        assertEquals(0,transport.sent); assertEquals(1,transport.loggedOut)
    }
}
