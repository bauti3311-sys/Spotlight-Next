package dev.bautista.spotlight.experimental.whatsapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import dev.bautista.spotlight.control.Replies
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.CancellationException
import java.nio.ByteBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Isolated prototype: outside app/src, not registered as an AI tool or an app capability.
// No provider selection, consent or account linking is enabled by importing this file.
enum class WhatsAppMessageState { REQUESTED, ACCEPTED, EXTERNAL_REVIEW, UNKNOWN }
data class WhatsAppReceipt(val state: WhatsAppMessageState, val messageId: String? = null)

class WhatsAppDraft(
    val text: String,
    val recipientE164: String? = null,
    val notificationKey: String? = null,
    val notificationTitle: String? = null,
    val conversationId: String? = null,
    val requestId: String = UUID.randomUUID().toString(),
) {
    init {
        require(text.isNotBlank() && text.length <= 8000)
        UUID.fromString(requestId)
    }
    internal fun fingerprint(provider: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        listOf(provider, requestId, recipientE164, notificationKey, notificationTitle, conversationId, text).forEach { value ->
            val bytes = value?.toByteArray(Charsets.UTF_8)
            digest.update(ByteBuffer.allocate(4).putInt(bytes?.size ?: -1).array())
            bytes?.let { digest.update(it) }
        }
        return digest.digest()
    }
    // Do not generate a data-class toString containing message text or contact identifiers.
}

class HumanApproval internal constructor(internal val nonce: String)

class HumanReviewGate(private val now: () -> Long = SystemClock::elapsedRealtime) {
    private data class Review(val digest: ByteArray, val deadline: Long)
    private val reviews = mutableMapOf<String, Review>()
    /** Trusted UI click only. Gemini receives no API to call this or submit approvals. */
    @Synchronized fun confirmFromUi(draft: WhatsAppDraft, providerId: String): HumanApproval {
        reviews.entries.removeAll { it.value.deadline <= now() }
        check(reviews.size < 20)
        val nonce = UUID.randomUUID().toString()
        reviews[nonce] = Review(draft.fingerprint(providerId), now() + 120_000)
        return HumanApproval(nonce)
    }
    @Synchronized internal fun consume(draft: WhatsAppDraft, providerId: String, approval: HumanApproval) {
        val review = reviews.remove(approval.nonce) ?: error("Revisá y confirmá el mensaje.")
        check(review.deadline > now() && MessageDigest.isEqual(review.digest, draft.fingerprint(providerId))) {
            "Cambió el texto, destinatario o proveedor; revisá nuevamente."
        }
    }
    @Synchronized fun cancelAll() { reviews.clear() }
}

interface WhatsAppMessagingProvider {
    val id: String
    fun available(draft: WhatsAppDraft): Boolean
    suspend fun submit(draft: WhatsAppDraft, approval: HumanApproval): WhatsAppReceipt
}

/** Private durable fence is committed BEFORE dispatch. Never reset it on timeouts/restarts. */
class SendFence(context: Context) {
    private val preferences = context.getSharedPreferences("whatsapp_send_fences", Context.MODE_PRIVATE)
    fun reserve(id: String): Boolean = synchronized(SendFence::class.java) {
        if (preferences.contains(id)) return false
        check(preferences.all.size < 10_000) { "Revisá el registro de envíos; no se borran recibos automáticamente." }
        check(preferences.edit().putBoolean(id, true).commit()) { "No se pudo guardar el registro; envío bloqueado." }
        true
    }
}

abstract class ConfirmedWhatsAppProvider(
    private val gate: HumanReviewGate, private val fence: SendFence,
) : WhatsAppMessagingProvider {
    final override suspend fun submit(draft: WhatsAppDraft, approval: HumanApproval): WhatsAppReceipt {
        check(available(draft)) { "La capacidad ya no está disponible." }
        gate.consume(draft, id, approval)
        check(withContext(Dispatchers.IO) { fence.reserve(draft.requestId) }) { "Solicitud ya procesada; revisá su estado, no la repitas." }
        return try { dispatch(draft) }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { WhatsAppReceipt(WhatsAppMessageState.UNKNOWN) }
    }
    protected abstract suspend fun dispatch(draft: WhatsAppDraft): WhatsAppReceipt
}

class NotificationReplyProvider(
    private val context: Context, gate: HumanReviewGate, fence: SendFence,
) : ConfirmedWhatsAppProvider(gate, fence) {
    override val id = "notification-reply"
    override fun available(draft: WhatsAppDraft): Boolean = Replies.connected.value &&
        Replies.targets.value.any { target ->
            target.key == draft.notificationKey && target.packageName == "com.whatsapp" && !target.group &&
                target.title == draft.notificationTitle && target.conversationId == draft.conversationId &&
                target.action?.remoteInputs?.any { it.allowFreeFormInput } == true
        }
    override suspend fun dispatch(draft: WhatsAppDraft): WhatsAppReceipt {
        check(available(draft))
        Replies.send(context, requireNotNull(draft.notificationKey), draft.text)
        return WhatsAppReceipt(WhatsAppMessageState.REQUESTED) // Not a delivery acknowledgement.
    }
}

interface LinkedDeviceTransport {
    suspend fun submit(draft: WhatsAppDraft): WhatsAppReceipt
    suspend fun logout(): Boolean
}

class LinkedDeviceMessagingProvider(
    private val transport: LinkedDeviceTransport, private val gate: HumanReviewGate, fence: SendFence,
    consented: Boolean = false,
) : ConfirmedWhatsAppProvider(gate, fence) {
    override val id = "linked-device-experimental"
    @Volatile private var enabled = consented
    private val dispatchLock = Mutex()
    override fun available(draft: WhatsAppDraft) = enabled && validPhone(draft.recipientE164)
    override suspend fun dispatch(draft: WhatsAppDraft): WhatsAppReceipt = dispatchLock.withLock {
        check(enabled)
        transport.submit(draft).also {
            check(it.state in setOf(WhatsAppMessageState.ACCEPTED, WhatsAppMessageState.UNKNOWN))
        }
    }
    suspend fun disconnect(): Boolean = dispatchLock.withLock {
        enabled = false
        gate.cancelAll()
        try { transport.logout() } catch (e: CancellationException) { throw e } catch (e: Exception) { false }
    }
}

class PublicIntentMessagingProvider(
    private val context: Context, gate: HumanReviewGate, fence: SendFence,
) : ConfirmedWhatsAppProvider(gate, fence) {
    override val id = "public-intent"
    private fun intent(draft: WhatsAppDraft) = Intent(Intent.ACTION_VIEW,
        Uri.Builder().scheme("https").authority("wa.me")
            .appendPath(requireNotNull(draft.recipientE164).removePrefix("+"))
            .appendQueryParameter("text", draft.text).build())
        .setPackage("com.whatsapp").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    override fun available(draft: WhatsAppDraft) = validPhone(draft.recipientE164) &&
        context.packageManager.resolveActivity(intent(draft), 0) != null
    override suspend fun dispatch(draft: WhatsAppDraft): WhatsAppReceipt {
        check(available(draft))
        context.startActivity(intent(draft))
        return WhatsAppReceipt(WhatsAppMessageState.EXTERNAL_REVIEW)
    }
}

private fun validPhone(value: String?) = value?.matches(Regex("\\+[1-9][0-9]{6,14}")) == true
