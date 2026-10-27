package dev.bautista.spotlight.control

import android.app.*
import android.content.*
import android.os.Bundle
import android.service.notification.StatusBarNotification
import dev.bautista.spotlight.search.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.time.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class ActionCatalogTest {
    @org.junit.After fun clearNotificationFixture() { Replies.clear() }
    @Test fun businessRepliesStayInsideSelectedApplication():Unit=runBlocking {
        val context=RuntimeEnvironment.getApplication();Replies.clear()
        fun notification(pkg:String,title:String,id:Int){
            val pi=PendingIntent.getBroadcast(context,id,Intent("reply:$pkg"),PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val remote=RemoteInput.Builder("reply_text").build()
            val n=Notification.Builder(context,"test").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).addAction(Notification.Action.Builder(null,"Responder",pi).addRemoteInput(remote).build()).build()
            Replies.put(StatusBarNotification(pkg,pkg,id,null,1000,0,0,n,android.os.Process.myUserHandle(),0))
        }
        notification("com.whatsapp","Personal",31);notification("com.whatsapp.w4b","Business",32);Replies.connected.value=true
        val taskScope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
        try {
            val prefs=dev.bautista.spotlight.data.Preferences(context)
            val engine=SearchEngine(context,prefs,dev.bautista.spotlight.data.LocalStore(prefs,taskScope),dev.bautista.spotlight.index.FileIndex(context))
            val rows=engine.search("",dev.bautista.spotlight.data.Config(files=false),SearchScope("whatsapp_business","replies")).first()
            assertEquals(listOf("Business"),rows.map{it.title});assertTrue(rows.single().action is Action.ReadNotification)
        } finally {taskScope.cancel();Replies.clear()}
    }
    @Test fun whatsappOffersActionsBeforeFiles() {
        val menu=ActionCatalog.menu("whatsapp")
        assertTrue(menu.any{it.action is Action.ComposeMessage})
        assertTrue(menu.none{it.category=="Archivos"})
        val scope=(menu.first{it.title=="Buscar archivos"}.action as Action.Navigate).scope
        assertEquals("files-menu",scope.section)
        assertEquals(7,ActionCatalog.fileMenu(scope).size)
        assertEquals("kind:image vacaciones",ActionCatalog.fileQuery(SearchScope("whatsapp","files","image"),"vacaciones"))
    }
    @Test fun exactTopicDoesNotStartFileIndexing() = runBlocking {
        val context=RuntimeEnvironment.getApplication()
        val prefs=dev.bautista.spotlight.data.Preferences(context)
        val taskScope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
        var scanned=false
        val index=dev.bautista.spotlight.index.FileIndex(context,{true},{scanned=true;emptyList()})
        try {
            val engine=SearchEngine(context,prefs,dev.bautista.spotlight.data.LocalStore(prefs,taskScope),index)
            val rows=engine.search("whatsapp",dev.bautista.spotlight.data.Config()).first()
            assertTrue(rows.none{it.action is Action.ComposeMessage});assertFalse(scanned)
            assertTrue(rows.none{it.category=="Archivos"})
            val volume=engine.search("volumen 150",dev.bautista.spotlight.data.Config()).first()
            assertTrue(volume.single().action is Action.RestoreQuery)
            assertTrue(volume.single().title.contains("entre 0 y 100"))
        } finally {taskScope.cancel()}
    }
    @Test fun naturalSettingsAndGoogleArePreparedWithoutManualLinks() {
        assertEquals(android.provider.Settings.ACTION_SOUND_SETTINGS,ActionCatalog.settings("Abrir ajustes de sonido")!!.action)
        assertEquals(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS,ActionCatalog.settings("abrir los ajustes de Bluetooth")!!.action)
        assertNull(ActionCatalog.settings("sonido"))
        val canonical=ActionCatalog.canonical("Buscar este tema en Google")
        assertEquals("google este tema",canonical)
        val action=ActionCatalog.searchAction("google","",canonical.substringAfter(' ')) as Action.Launch
        assertEquals("este tema",action.intent.data!!.getQueryParameter("q"))
    }
    @Test fun calendarRequestsItsOwnPermission() = runBlocking {
        val context=RuntimeEnvironment.getApplication()
        Shadows.shadowOf(context).denyPermissions(android.Manifest.permission.READ_CALENDAR)
        assertEquals("calendar",(Agenda.search(context,"","today").single().action as Action.Access).access)
    }
    @Test fun naturalMessagePreservesCaseAndPunctuation() {
        val draft=ActionCatalog.message("enviar mensaje a Ene diciendo Hola, ¿Venís mañana?")!!
        assertEquals("Ene",draft.target);assertEquals("Hola, ¿Venís mañana?",draft.text)
        assertEquals("email",ActionCatalog.message("correo a Ana: Mi informe")!!.channel)
        assertNull(ActionCatalog.message("whatsapp"))
    }
    @Test fun publicSearchUsesEncodedQueryAndPreferredApp() {
        assertEquals("youtube Perros en la playa",ActionCatalog.canonical("buscar Perros en la playa en youtube"))
        assertEquals("vivaldi Kotlin Compose",ActionCatalog.canonical("buscame Kotlin Compose en vivaldi"))
        assertEquals("chrome Android 15",ActionCatalog.canonical("busca en chrome: Android 15"))
        val action=ActionCatalog.searchAction("youtube","com.google.android.youtube","a&b fútbol") as Action.InApp
        assertEquals(Intent.ACTION_VIEW,action.intent.action)
        assertEquals("com.google.android.youtube",action.intent.`package`)
        assertEquals("a&b fútbol",action.fallback.data!!.getQueryParameter("search_query"))
    }
    @Test fun messageLinksAreGeneratedWithoutConfiguration() {
        val c=RuntimeEnvironment.getApplication()
        val action=People.messageIntent(c,"whatsapp","+54 9 11 1234 5678","Hola & chau")!!
        assertEquals("/5491112345678",action.data!!.path)
        assertEquals("Hola & chau",action.data!!.getQueryParameter("text"))
        assertNull(People.international(c,"+00000000"))
        assertEquals(Intent.ACTION_SENDTO,People.messageIntent(c,"sms","12345","hola")!!.action)
    }
    @Test fun duplicateNamesAndMultipleNumbersRequireSelection() {
        val a=Person(1,"Ene","+5491111111111")
        assertEquals(a,People.uniqueExact(listOf(a),"ene"))
        assertNull(People.uniqueExact(listOf(a,a.copy(id=2,address="+5491122222222")),"Ene"))
        assertNull(People.uniqueExact(listOf(a,a.copy(address="+5491133333333")),"Ene"))
        assertNull(People.uniqueExact(listOf(a),"E"))
    }
    @Test fun agendaRangeUsesLocalDaysAcrossDaylightSaving() {
        val zone=ZoneId.of("America/New_York");val date=LocalDate.of(2026,3,8)
        val day=Agenda.range("today",date,zone)
        assertEquals(23*60*60*1000L,day.second-day.first)
        assertEquals(day.second,Agenda.range("tomorrow",date,zone).first)
        assertEquals(date.plusDays(7).atStartOfDay(zone).toInstant().toEpochMilli(),Agenda.range("week",date,zone).second)
    }
    @Test fun remoteReplyUsesNotificationPendingIntentAndInvalidatesTarget() {
        val context=RuntimeEnvironment.getApplication();Replies.clear()
        val pi=PendingIntent.getBroadcast(context,99,Intent("test.reply"),PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val remote=RemoteInput.Builder("reply_text").setLabel("Responder").build()
        val notification=Notification.Builder(context,"test").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Ene").addAction(Notification.Action.Builder(null,"Responder",pi).addRemoteInput(remote).build()).build()
        val sbn=StatusBarNotification("com.whatsapp","com.whatsapp",1,null,1000,0,0,notification,android.os.Process.myUserHandle(),0)
        Replies.put(sbn);Replies.connected.value=true;assertEquals(1,Replies.targets.value.size)
        Replies.send(context,sbn.key,"Hola Ene")
        val sent=Shadows.shadowOf(context).broadcastIntents.last{it.action=="test.reply"}
        assertEquals("Hola Ene",RemoteInput.getResultsFromIntent(sent)!!.getCharSequence("reply_text"))
        assertTrue(Replies.targets.value.isEmpty())
        assertThrows(IllegalStateException::class.java){Replies.send(context,sbn.key,"otra")}
    }
}
