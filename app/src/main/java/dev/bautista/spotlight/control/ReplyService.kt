package dev.bautista.spotlight.control

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.*
import android.graphics.drawable.Icon
import android.os.Bundle
import android.os.Build
import android.service.notification.*
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.flow.*

data class ReceivedMessage(val text:String,val sender:String="",val time:Long=0)
data class ReplyTarget(val key:String,val title:String,val packageName:String,val action:Notification.Action?,val messages:List<ReceivedMessage> = emptyList(),val time:Long=0,val group:Boolean=false,val icon:Icon?=null,val open:PendingIntent?=null,val conversationId:String?=null) {
    val visibleText:String get()=messages.joinToString("\n"){if(group && it.sender.isNotBlank())"${it.sender}: ${it.text}" else it.text}
}
/** Notification content stays in RAM and is removed with its source notification. */
object Replies {
    private val entries=linkedMapOf<String,ReplyTarget>()
    private val mutable=MutableStateFlow<List<ReplyTarget>>(emptyList())
    val targets=mutable.asStateFlow()
    val connected=MutableStateFlow(false)
    private fun publish(){
        mutable.value=entries.values.sortedByDescending{it.time}.distinctBy{it.packageName+":"+(it.conversationId ?: it.key)}.take(40)
    }
    @Synchronized fun put(notification:StatusBarNotification) {
        val n=notification.notification
        if(notification.packageName !in setOf("com.whatsapp","com.whatsapp.w4b") || n.flags and Notification.FLAG_GROUP_SUMMARY !=0){remove(notification.key);return}
        val e=n.extras
        val style=runCatching{androidx.core.app.NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(n)}.getOrNull()
        val title=(style?.conversationTitle ?: e.getCharSequence(Notification.EXTRA_TITLE))?.toString()?.trim()?.take(200).orEmpty()
        if(title.isBlank()){remove(notification.key);return}
        val styled=style?.messages.orEmpty().takeLast(20).mapNotNull{m->m.text?.toString()?.take(4000)?.takeIf{it.isNotBlank()}?.let{ReceivedMessage(it,m.sender?.toString()?.take(200).orEmpty(),m.timestamp)}}
        val lines=e.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.takeLast(20)?.mapNotNull{it?.toString()?.take(4000)?.takeIf{t->t.isNotBlank()}?.let{t->ReceivedMessage(t,time=notification.postTime)}}.orEmpty()
        val fallback=(e.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: e.getCharSequence(Notification.EXTRA_TEXT))?.toString()?.take(4000)
        val messages=(if(styled.isNotEmpty())styled else if(lines.isNotEmpty())lines else listOfNotNull(fallback?.takeIf{it.isNotBlank()}?.let{ReceivedMessage(it,time=notification.postTime)})).distinctBy{Triple(it.text,it.sender,it.time)}
        val action=n.actions?.firstOrNull{it.remoteInputs?.any{input->input.allowFreeFormInput}==true}
        val isGroup=if(Build.VERSION.SDK_INT>=28)style?.isGroupConversation==true else style?.conversationTitle!=null
        entries[notification.key]=ReplyTarget(notification.key,title,notification.packageName,action,messages,notification.postTime,isGroup,n.getLargeIcon(),n.contentIntent,n.shortcutId?.takeIf{it.isNotBlank()})
        while(entries.size>40)entries.remove(entries.values.minBy{it.time}.key)
        publish()
    }
    @Synchronized fun remove(key:String){entries.remove(key);publish()}
    @Synchronized fun clear(){entries.clear();mutable.value=emptyList();connected.value=false}
    fun parseReplyQuery(raw:String):Pair<String,String>? {
        val text=raw.trim().take(8000);val norm=normalize(text)
        if(norm=="responder el ultimo mensaje visible de whatsapp")return "" to ""
        Regex("^(?:responder(?:le)?|contestar(?:le)?)(?: un?(?: mensaje| whatsapp)?)?(?: por whatsapp)?(?: a)? (.+?)(?: que | diciendo:? |: ?)(.+)$",RegexOption.IGNORE_CASE).matchEntire(text)?.let {
            return it.groupValues[1].trim() to it.groupValues[2].trim()
        }
        Regex("^(?:responder(?:le)?|contestar(?:le)?)(?: un?(?: mensaje| whatsapp)?)?(?: por whatsapp)?(?: a)? (.+)$",RegexOption.IGNORE_CASE).matchEntire(text)?.let {
            return it.groupValues[1].trim() to ""
        }
        return null
    }
    fun findCompatibleReply(channel:String,contactQuery:String):ReplyTarget? {
        if(!connected.value || contactQuery.isBlank())return null
        val pkg=when(channel){"whatsapp"->"com.whatsapp";"whatsapp_business"->"com.whatsapp.w4b";else->return null}
        val q=normalize(contactQuery)
        return targets.value.filter{it.packageName==pkg && !it.group && it.action!=null && normalize(it.title)==q}.singleOrNull()
    }
    fun results(query:String="",packageName:String="com.whatsapp",reply:Boolean=false,draftText:String=""):List<Result> = targets.value.filter{(packageName.isBlank() || it.packageName==packageName) && (query.isBlank() || normalize(it.title).contains(normalize(query)) || normalize(it.visibleText).contains(normalize(query)))}.map{t->val channel=if(t.packageName=="com.whatsapp.w4b")"whatsapp_business" else "whatsapp";Result("reply:${t.key}",t.title,if(t.group)"Grupo · notificación actual" else "${if(channel=="whatsapp_business")"WhatsApp Business" else "WhatsApp"} · notificación actual","Mensajes",2200,if(reply && t.action!=null)Action.ComposeMessage(channel,t.title,text=draftText,replyKey=t.key) else Action.ReadNotification(t.key),preview=t.visibleText.ifBlank{"La notificación no contiene texto accesible."})}
    @Synchronized fun send(context:Context,key:String,text:String,expected:ReplyTarget?=null) {
        require(text.isNotBlank() && text.length<=8000)
        check(connected.value){"El servicio de notificaciones está desconectado. Revisá el acceso antes de responder."}
        val target=targets.value.firstOrNull{it.key==key} ?: error("La notificación ya no está disponible")
        if(expected!=null)check(target.key==expected.key && target.packageName==expected.packageName &&
            target.title==expected.title && target.conversationId==expected.conversationId && target.group==expected.group &&
            target.action?.actionIntent==expected.action?.actionIntent &&
            target.action?.remoteInputs?.map{it.resultKey to it.allowFreeFormInput}==
                expected.action?.remoteInputs?.map{it.resultKey to it.allowFreeFormInput}){"La conversación o su acción cambió. Revisá y confirmá nuevamente."}
        val action=target.action ?: error("Esta notificación no admite respuesta directa")
        val inputs=action.remoteInputs?.filter{it.allowFreeFormInput}?.toTypedArray()?.takeIf{it.isNotEmpty()} ?: error("No admite respuesta")
        val values=Bundle();inputs.forEach{values.putCharSequence(it.resultKey,text.take(8000))}
        val fill=Intent();RemoteInput.addResultsToIntent(inputs,fill,values)
        // Consume this notification locally before the external effect. An uncertain failure never restores approval.
        remove(key)
        try{action.actionIntent.send(context,0,fill)}catch(e:PendingIntent.CanceledException){error("La acción de respuesta venció. Abrí WhatsApp para continuar.")}
    }
}
class ReplyService:NotificationListenerService() {
    override fun onListenerConnected(){Replies.clear();Replies.connected.value=true;activeNotifications?.forEach{Replies.put(it)}}
    override fun onListenerDisconnected(){Replies.clear()}
    override fun onNotificationPosted(sbn:StatusBarNotification){Replies.put(sbn)}
    override fun onNotificationRemoved(sbn:StatusBarNotification){Replies.remove(sbn.key)}
    override fun onDestroy(){Replies.clear();super.onDestroy()}
}
