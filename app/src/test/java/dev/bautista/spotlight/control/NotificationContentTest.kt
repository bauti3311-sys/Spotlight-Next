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
class NotificationContentTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private fun builder(title:String="Juan")=Notification.Builder(context,"test").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title)
    private fun put(n:Notification,id:Int=1):String {val s=StatusBarNotification("com.whatsapp","com.whatsapp",id,null,1000,0,0,n,android.os.Process.myUserHandle(),1700000000000+id);Replies.put(s);return s.key}
    @Before fun reset(){Replies.clear();Replies.connected.value=true}
    @After fun clean(){Replies.clear()}
    @Test fun individualWithoutRemoteInputStillReadable(){put(builder().setContentText("Nos vemos a las 18").build());assertEquals("Nos vemos a las 18",Replies.targets.value.single().visibleText);assertNull(Replies.targets.value.single().action)}
    @Test fun messagingStyleIncludesMessagesSendersAndGroup(){val style=Notification.MessagingStyle(android.app.Person.Builder().setName("Yo").build()).setConversationTitle("Estudio").setGroupConversation(true).addMessage("Hola",100,android.app.Person.Builder().setName("Juan").build()).addMessage("Llevo el PDF",200,android.app.Person.Builder().setName("Ana").build());put(builder().setStyle(style).build());val t=Replies.targets.value.single();assertTrue(t.group);assertEquals(2,t.messages.size);assertEquals("Juan: Hola\nAna: Llevo el PDF",t.visibleText)}
    @Test fun bigTextFallbackAndBoundedLongContent(){val n=builder().build();n.extras.putCharSequence(Notification.EXTRA_BIG_TEXT,"x".repeat(9000));put(n);assertEquals(4000,Replies.targets.value.single().visibleText.length)}
    @Test fun textLinesFallbackDeduplicates(){val n=builder().build();n.extras.putCharSequenceArray(Notification.EXTRA_TEXT_LINES,arrayOf("Uno","Dos","Dos"));put(n);assertEquals(listOf("Uno","Dos"),Replies.targets.value.single().messages.map{it.text})}
    @Test fun summariesAreNotMessages(){put(builder().setGroup("all").setGroupSummary(true).setContentText("3 mensajes").build());assertTrue(Replies.targets.value.isEmpty())}
    @Test fun inaccessibleMessageIsExplicitAndRemovedWithNotification(){val key=put(builder().build());assertTrue(Replies.targets.value.single().messages.isEmpty());assertTrue(Replies.results().single().preview!!.contains("no contiene texto"));Replies.remove(key);assertTrue(Replies.targets.value.isEmpty())}
    @Test fun disconnectClearsContentAndAccessState(){put(builder().setContentText("Privado").build());Replies.clear();assertFalse(Replies.connected.value);assertTrue(Replies.targets.value.isEmpty())}
    @Test fun sameNamedContactsRemainDistinct(){put(builder().setContentText("Uno").build(),1);put(builder().setContentText("Dos").build(),2);assertEquals(2,Replies.targets.value.size)}
    @Test fun sameConversationDoesNotDuplicate(){put(builder().setShortcutId("conversation-juan").setContentText("Anterior").build(),1);put(builder().setShortcutId("conversation-juan").setContentText("Nuevo").build(),2);assertEquals(1,Replies.targets.value.size);assertEquals("Nuevo",Replies.targets.value.single().visibleText)}
    @Test fun canceledReplyFailsClearlyAndInvalidates(){val pi=PendingIntent.getBroadcast(context,55,Intent("reply.cancel"),PendingIntent.FLAG_MUTABLE);val remote=RemoteInput.Builder("body").build();val key=put(builder().setContentText("Hola").addAction(Notification.Action.Builder(null,"Responder",pi).addRemoteInput(remote).build()).build());pi.cancel();assertThrows(IllegalStateException::class.java){Replies.send(context,key,"Hola")};assertTrue(Replies.targets.value.isEmpty())}
    @Test fun noRemoteActionCannotSend(){val key=put(builder().setContentText("Hola").build());assertThrows(IllegalStateException::class.java){Replies.send(context,key,"Respuesta")}}
}
