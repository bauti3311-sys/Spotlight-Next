package dev.bautista.spotlight.search

import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.*
import dev.bautista.spotlight.automation.*
import dev.bautista.spotlight.personal.PersonalCommands
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=SpotlightApp::class)
class Next091ProductivityTest {
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    @Test fun textOperationsHandleUnicodeAndMultiline(){assertEquals("3",TextTools.transform("contar caracteres","a😀b"));assertEquals("2",TextTools.transform("contar palabras","¿Cómo estás?"));assertEquals("a\nz",TextTools.transform("ordenar lineas","z\na"));assertEquals("A\nB",TextTools.transform("quitar duplicados","A\nA\nB"));assertEquals("Hola mundo\nOtra línea",TextTools.transform("limpiar espacios","  Hola   mundo  \n Otra\tlínea "));assertEquals("rehabilitacion-vestibular",TextTools.transform("slug","Rehabilitación vestibular"))}
    @Test fun textValidationAndRoundTrips(){val raw="¿Hola? a+b 😀";assertEquals(raw,TextTools.transform("decodificar url",TextTools.transform("codificar url",raw)));assertTrue(TextTools.transform("formatear json","{\"a\":1}").contains("\n"));assertThrows(Exception::class.java){TextTools.transform("formatear json","{bad}")};assertThrows(IllegalArgumentException::class.java){TextTools.transform("mayusculas","x".repeat(16001))}}
    @Test fun localCardsNeverNeedOnlineProvider():Unit=runBlocking{val card=app.online.lookup(Action.Lookup("text:mayusculas","hola"),Config(online=false));assertEquals("HOLA",card.copyText);assertNull(card.url);val uuid=app.online.lookup(Action.Lookup("uuid",""),Config());assertNotNull(java.util.UUID.fromString(uuid.copyText))}
    @Test fun commandQueriesDoNotGetMixedWithFiles():Unit=runBlocking{val rows=app.search.search("mayúsculas: hola",Config(files=true,online=false)).last();assertEquals(1,rows.size);assertEquals("text:mayusculas",(rows.single().action as Action.Lookup).type);assertEquals("Herramientas",rows.single().category)}
    @Test fun modifiersUseRealScopesAndPermissionFallback():Unit=runBlocking{assertEquals("pdf",SearchModifiers.parse("/pdf anatomía")!!.scope.kind);assertEquals("Juan",SearchModifiers.parse("Contactos Juan")!!.query);val rows=app.search.search("/contacts Juan",Config(contacts=false,files=false)).last();assertTrue(rows.single().action is Action.Access);val ai=app.search.search("/ai explicá anatomía",Config(files=false)).last();assertEquals("explicá anatomía",(ai.single().action as Action.Ask).prompt);assertNull(SearchModifiers.parse("/unknown test"))}
    @Test fun snippetsAskForFieldsInsteadOfCopyingUnresolvedText(){val template="Hola {nombre}, hoy {fecha} a las {hora}: {entrada}";assertEquals(listOf("nombre","entrada"),PersonalCommands.fields(template));val rows=PersonalCommands.results("snippets",Config(snippets=setOf("saludo|$template")));assertTrue(rows.single().action is Action.ExpandSnippet);val filled=PersonalCommands.fill(template,mapOf("nombre" to "Juan","entrada" to "bienvenido"));assertFalse(filled.contains("{"));assertTrue(filled.startsWith("Hola Juan"))}
    @Test fun clipboardIdentityAndFavoritesStayTemporary(){app.local.clearClips();app.local.capture("uno");app.local.capture("dos");val first=LocalStore.clipId("uno");app.local.toggleClipFavorite("uno");app.local.editClip("uno","tres");assertFalse(app.local.clips.value.contains("uno"));assertTrue("tres" in app.local.clipFavorites.value);assertEquals(first,LocalStore.clipId("uno"));assertNotEquals(first,LocalStore.clipId("dos"));app.local.clearClips();assertTrue(app.local.clipFavorites.value.isEmpty())}
    private class Host:WorkflowHost{val copies=mutableListOf<String>();override suspend fun copy(text:String){copies+=text};override suspend fun note(text:String){};override suspend fun volume(level:Int){};override suspend fun timer(minutes:Int){}}
    @Test fun workflowConditionStopsBeforeSideEffects():Unit=runBlocking{val host=Host();val w=Workflow("x","X",steps=listOf(WorkflowStep("require_text"),WorkflowStep("copy")));val events=Workflows.run(w,host).toList();assertTrue(events.last().failed);assertTrue(host.copies.isEmpty())}
    @Test fun workflowPassesUserInputThroughLocalTransforms():Unit=runBlocking{val host=Host();val w=Workflow("x","X",steps=listOf(WorkflowStep("require_text"),WorkflowStep("clean"),WorkflowStep("dedupe"),WorkflowStep("copy")));assertTrue(Workflows.run(w,host,"  Hola   mundo \n  Hola mundo ").last().complete);assertEquals(listOf("Hola mundo"),host.copies)}
    @Test fun symbolsAreCopyableDataNotNavigation(){assertEquals("→",(SymbolTools.results("símbolo flecha derecha").single().action as Action.Copy).text);assertTrue(SymbolTools.results("emoji desconocido").isEmpty())}
    @Test fun musicCategoryUsesDeclaredAppMetadata():Unit=runBlocking {
        val launcher=android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        val activity=android.content.pm.ActivityInfo().apply{packageName="fixture.audio";name="fixture.audio.Main";nonLocalizedLabel="Mi música";applicationInfo=android.content.pm.ApplicationInfo().apply{packageName="fixture.audio";category=android.content.pm.ApplicationInfo.CATEGORY_AUDIO}}
        Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(launcher,android.content.pm.ResolveInfo().apply{activityInfo=activity})
        app.search.invalidateApps();val rows=app.search.search("aplicaciones de música",Config(files=false)).last();assertTrue(rows.any{it.title=="Mi música"});assertTrue(rows.all{it.category=="Aplicaciones"})
    }

}
