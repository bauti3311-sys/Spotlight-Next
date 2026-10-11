package dev.bautista.spotlight.search

import android.app.Application
import android.content.Intent
import android.provider.CalendarContract
import dev.bautista.spotlight.automation.*
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.ui.AppearancePresets
import dev.bautista.spotlight.overlay.AdaptiveLayout
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config as RoboConfig
import java.time.*

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class NextToolsTest {
    private class Host:WorkflowHost {val calls=mutableListOf<String>();override suspend fun copy(text:String){calls+="copy:$text"};override suspend fun note(text:String){calls+="note:$text"};override suspend fun volume(level:Int){calls+="volume:$level"};override suspend fun timer(minutes:Int){calls+="timer:$minutes"}}
    @Test fun workflowRoundTripsAndPassesData():Unit=runBlocking {
        val w=Workflow("study","Estudio","estudiar",listOf(WorkflowStep("text","hola"),WorkflowStep("upper"),WorkflowStep("copy"),WorkflowStep("note","Nota: {entrada}")))
        assertEquals(listOf(w),Workflows.parse(Workflows.encode(listOf(w))))
        val host=Host();val events=Workflows.run(w,host).toList();assertTrue(events.last().complete);assertEquals(listOf("copy:HOLA","note:Nota: HOLA"),host.calls)
        assertEquals(1,Workflows.results("",Workflows.encode(listOf(w)),setOf("routine:study")).size)
        assertTrue(Workflows.results("",Workflows.encode(listOf(w)),emptySet()).isEmpty())
    }
    @Test fun workflowRejectsInvalidSteps(){assertNotNull(Workflows.validate(Workflow("x","X",steps=listOf(WorkflowStep("volume","150")))));assertNotNull(Workflows.validate(Workflow("x","X",steps=listOf(WorkflowStep("timer","25"),WorkflowStep("copy")))));assertTrue(Workflows.parse("bad json").isEmpty())}
    @Test fun workflowStopsAfterFailedStep():Unit=runBlocking {val host=object:WorkflowHost{override suspend fun copy(text:String){error("fail")};override suspend fun note(text:String){fail("must not run")};override suspend fun volume(level:Int){};override suspend fun timer(minutes:Int){}};val events=Workflows.run(Workflow("x","X",steps=listOf(WorkflowStep("copy"),WorkflowStep("note"))),host).toList();assertTrue(events.last().failed);assertEquals(1,events.last().step)}
    @Test fun workflowCancellationStopsPendingActions():Unit=runBlocking {val host=Host();val job=launch{Workflows.run(Workflow("x","X",steps=listOf(WorkflowStep("wait","30"),WorkflowStep("note"))),host).collect{if(it.step==1)cancel()}};job.join();assertTrue(host.calls.isEmpty())}
    @Test fun phoneAndTabletBoundsReserveImeAndStayOnScreen(){val phone=AdaptiveLayout.bounds(360,800,1f,300,Config());assertEquals(336,phone.width);assertEquals(64,phone.top);assertEquals(412f,phone.heightDp);val tablet=AdaptiveLayout.bounds(1280,800,1f,0,Config());assertEquals(640,tablet.width);assertEquals(128,tablet.top)}
    @Test fun presetsPreserveNonVisualData(){val c=Config(trees=setOf("content://tree"),snippets=setOf("firma|Texto"),aiProvider="gemini",routines="[]");AppearancePresets.names.forEach{val next=AppearancePresets.apply(it,c);assertEquals(c.trees,next.trees);assertEquals(c.snippets,next.snippets);assertEquals(c.aiProvider,next.aiProvider)}}
    @Test fun naturalFilesEventsAndCalls(){assertEquals("kind:pdf anatomia",SearchQuery.natural("PDF anatomía"));assertEquals("kind:pdf modified:ayer",SearchQuery.natural("buscar PDF de ayer"));assertEquals("mamá",NaturalIntents.callName("llamar a mamá"));val intent=NaturalIntents.event("crear evento mañana a las 10",LocalDate.of(2026,10,10),ZoneId.of("America/Argentina/Buenos_Aires"))!!;assertEquals(Intent.ACTION_INSERT,intent.action);assertEquals(Instant.parse("2026-10-11T13:00:00Z").toEpochMilli(),intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,0));assertNull(NaturalIntents.event("crear evento hoy a las 30"))}
    @Test fun explicitMessagingChannelOverridesDefault(){assertEquals("sms",ActionCatalog.message("enviar un mensaje a Juan: Hola","sms")!!.channel);assertEquals("whatsapp",ActionCatalog.message("mensaje por whatsapp a Juan: Hola","sms")!!.channel);assertEquals("whatsapp_business",ActionCatalog.message("whatsapp business a Juan: Hola")!!.channel);assertEquals("com.whatsapp.w4b",People.messageIntent(RuntimeEnvironment.getApplication(),"whatsapp_business","+5491112345678","Hola")!!.`package`)}
    @Test fun areaSpeedAndCubicVolumeRemainOffline(){assertEquals("1 ha",LocalTools.convert("10000 m² a ha")!!.title);assertEquals("10 m/s",LocalTools.convert("36 km/h a m/s")!!.title);assertEquals("1000 litros",LocalTools.convert("1 m³ a litros")!!.title);assertNull(LocalTools.convert("5 m² a kg"))}
}
