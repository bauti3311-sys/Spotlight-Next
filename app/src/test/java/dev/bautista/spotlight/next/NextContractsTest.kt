package dev.bautista.spotlight.next

import android.app.Application
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.automation.Workflows
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class NextContractsTest {
    @Test fun messagePreservesBodyAndDestination(){val action=IntentRouter.route("Mandale WhatsApp a Juan Pérez que llego a las ocho") as Action.ComposeMessage;assertEquals("Juan Pérez",action.target);assertEquals("llego a las ocho",action.text)}
    @Test fun accentedSpanishCommandsKeepOriginalText(){val note=IntentRouter.route("Creá una nota con este texto Hola ágil") as Action.EditNote;assertEquals("Hola ágil",note.text);assertEquals("Juan",(IntentRouter.route("Mandá WhatsApp a Juan que llego") as Action.ComposeMessage).target)}
    @Test fun genericMessageRespectsDefaultApp(){assertEquals("sms",(IntentRouter.route("Mandar mensaje a Juan que llego", "sms") as Action.ComposeMessage).channel);assertEquals("sms",(IntentRouter.route("Mandar mensaje a Juan que revisa WhatsApp", "sms") as Action.ComposeMessage).channel);assertEquals("whatsapp",(IntentRouter.route("Mandar WhatsApp a Juan que llego", "sms") as Action.ComposeMessage).channel);assertEquals("whatsapp",(IntentRouter.route("Mandar WhatsApp a App que llego", "sms") as Action.ComposeMessage).channel)}
    @Test fun messageWithoutBodyOpensEditor(){val action=IntentRouter.route("Mandale WhatsApp a Juan") as Action.ComposeMessage;assertEquals("Juan",action.target);assertEquals("",action.text)}
    @Test fun outOfRangeBrightnessDoesNotExecute(){assertNull(IntentRouter.route("Bajá brillo al 101%"));assertEquals(Action.SystemPanel("display-review","screen_brightness:102"),IntentRouter.route("Bajá brillo al 40%"))}
    @Test fun weatherIsNativeWithoutNetworkDuringRouting(){assertEquals(Action.SystemPanel("weather-native","mar del plata"),IntentRouter.route("Va a llover mañana en Mar del Plata"))}
    @Test fun pdfSearchIsScoped(){val action=IntentRouter.route("Buscá PDF de anatomía") as Action.Navigate;assertEquals("pdf",action.scope.kind);assertEquals("anatomia",action.scope.topic)}
    @Test fun unrecognizedQueryRemainsOrdinarySearch(){assertNull(IntentRouter.route("un archivo cualquiera"));assertNull(IntentRouter.route("apagar todo"))}
    @Test fun taskerContractUsesDocumentedParameters(){val intent=TaskerContract.taskIntent("net.dinglisch.android.taskerm","Spotlight Estudio",listOf("45","Juan"),"test-id");assertEquals("id:test-id",intent.data.toString());assertEquals(listOf("%par1","%par2"),intent.getStringArrayListExtra("varNames"));assertEquals("Spotlight Estudio",intent.getStringExtra("task_name"));assertEquals("1.1",intent.getStringExtra("version_number"))}
    @Test fun taskerRejectsArbitraryPackages(){try{TaskerContract.taskIntent("malicious.package","name",emptyList());fail()}catch(e:IllegalArgumentException){}}
    @Test fun profilesAreValidWorkflows(){listOf("estudio","lectura","concentracion","multimedia").forEach {assertNull(Workflows.validate(NativeCatalog.profile(it)!!))};assertNull(NativeCatalog.profile("unknown"))}
    @Test fun catalogHasUniqueIdentifiers(){assertEquals(NativeCatalog.items.size,NativeCatalog.items.map{it.id}.toSet().size);assertEquals(30,NativeCatalog.items.size)}
    @Test fun disabledCatalogItemDisappearsFromSearch(){val app=RuntimeEnvironment.getApplication();val prefs=app.getSharedPreferences("catalog_v1",0);prefs.edit().putStringSet("disabled",setOf("study")).commit();assertTrue(NativeCatalog.results("Modo estudio",app).none{it.id=="native:catalog:study"});prefs.edit().clear().commit()}
    @Test fun missingSportsScoreIsNotInvented(){assertEquals("Marcador no informado",ProviderValues.score(JSONObject().put("intHomeScore",JSONObject.NULL).put("intAwayScore",JSONObject.NULL)));assertEquals("0 — 0",ProviderValues.score(JSONObject().put("intHomeScore","0").put("intAwayScore","0")))}
    @Test fun missingWeatherDoesNotBecomeZero(){assertEquals("—",ProviderValues.number(JSONObject(),"temperature_2m","°"));assertEquals("0°",ProviderValues.number(JSONObject().put("temperature_2m",0),"temperature_2m","°"))}
}
