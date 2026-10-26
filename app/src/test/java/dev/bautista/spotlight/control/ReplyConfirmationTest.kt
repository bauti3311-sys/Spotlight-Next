package dev.bautista.spotlight.control

import android.app.*
import android.content.*
import android.service.notification.StatusBarNotification
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class ReplyConfirmationTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private fun put(title: String = "Juan", conversation: String = "juan", requestCode: Int = 1): ReplyTarget {
        val pi = PendingIntent.getBroadcast(context, requestCode, Intent("reply.fixture.$requestCode"), PendingIntent.FLAG_MUTABLE)
        val action = Notification.Action.Builder(null, "Responder", pi).addRemoteInput(RemoteInput.Builder("body").build()).build()
        val n = Notification.Builder(context, "test").setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title).setContentText("Fixture").setShortcutId(conversation).addAction(action).build()
        Replies.put(StatusBarNotification("com.whatsapp", "com.whatsapp", 1, null, 1000, 0, 0, n, android.os.Process.myUserHandle(), 1700000000000))
        return Replies.targets.value.single()
    }
    @Before fun reset() { Replies.clear(); Replies.connected.value = true }
    @After fun clean() { Replies.clear() }
    @Test fun changedConversationOrRecipientRequiresAnotherHumanReview() {
        for (replace in listOf<() -> Unit>({put(title="Ana")}, {put(conversation="other")} , {put(requestCode=2)})) {
            Replies.clear(); Replies.connected.value = true
            val reviewed = put(); replace()
            assertThrows(IllegalStateException::class.java) { Replies.send(context, reviewed.key, "Texto aprobado", reviewed) }
            assertEquals(1, Replies.targets.value.size)
        }
    }
    @Test fun reviewedTargetDispatchesOnceAndCannotBeRepeated() {
        val reviewed = put()
        Replies.send(context, reviewed.key, "Texto aprobado", reviewed)
        assertTrue(Replies.targets.value.isEmpty())
        assertThrows(IllegalStateException::class.java) { Replies.send(context, reviewed.key, "Texto aprobado", reviewed) }
    }
    @Test fun missingTargetAndDisconnectedListenerNeverDispatch() {
        val reviewed = put(); Replies.connected.value = false
        assertThrows(IllegalStateException::class.java) { Replies.send(context, reviewed.key, "Texto aprobado", reviewed) }
        Replies.connected.value = true; Replies.remove(reviewed.key)
        assertThrows(IllegalStateException::class.java) { Replies.send(context, reviewed.key, "Texto aprobado", reviewed) }
    }
}
