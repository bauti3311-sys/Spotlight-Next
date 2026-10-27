package dev.bautista.spotlight.search

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.data.LocalStore
import dev.bautista.spotlight.data.Preferences
import dev.bautista.spotlight.extensions.ExtensionRegistry
import dev.bautista.spotlight.index.FileIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class UniversalIntelligenceArchitectureTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val day = LocalDate.of(2026, 10, 15)
    private val zone = ZoneId.of("America/Argentina/Buenos_Aires")

    @After
    fun cleanup() {
        scope.cancel()
    }

    private fun engine(): SearchEngine {
        val prefs = Preferences(context)
        return SearchEngine(context, prefs, LocalStore(prefs, scope), FileIndex(context))
    }

    @Test
    fun phase1And3_bocaLibertadoresQuestionOffersGeminiPrimaryInsteadOfGoogleOnly() = runBlocking {
        val geminiConfig = AppConfig(files = false, contacts = false, aiProvider = "gemini", aiModel = "gemini-3.8-flash")
        for (query in listOf(
            "¿Cuántas veces ganó Boca la Libertadores?",
            "cuantas veces gano boca la libertadores",
            "¿Cuántas Libertadores ganó Boca?",
            "quien escribio el aleph",
            "donde queda mar del plata",
        )) {
            assertTrue("Should detect question: $query", LocalLanguage.isQuestion(query))
            val resolution = UniversalIntentEngine.resolve(query, geminiConfig, day, zone)
            assertEquals(PrimaryIntentKind.KNOWLEDGE_AI, resolution.primaryIntent)
            assertEquals(RemoteAiEscalation.EXPLICIT_PRIMARY, resolution.aiEscalation)
            assertEquals("Responder con Gemini", resolution.presentation.primaryTitle)

            val rows = engine().search(query, geminiConfig).toList().last()
            val first = rows.first()
            assertEquals("universal:ai", first.id)
            assertEquals("Responder con Gemini", first.title)
            assertTrue(first.action is Action.Ask)
            assertEquals(query, (first.action as Action.Ask).prompt)
        }
    }

    @Test
    fun naturalMathQuestionRemainsOfflineEvenWithGeminiConfigured() = runBlocking {
        val config = AppConfig(files = false, contacts = false, aiProvider = "gemini")
        val query = "¿Cuánto es 15% de 48.000?"
        val resolution = UniversalIntentEngine.resolve(query, config, day, zone)
        assertEquals(PrimaryIntentKind.LOCAL_TOOL, resolution.primaryIntent)
        assertEquals(RemoteAiEscalation.NONE, resolution.aiEscalation)
        assertEquals("7200", resolution.presentation.primaryTitle)
        val rows = engine().search(query, config).toList().last()
        assertTrue(rows.any { it.id == "calc" && it.title == "7200" && it.action is Action.Copy })
        assertFalse(rows.any { it.action is Action.Ask })
    }

    @Test
    fun naturalUnitQuestionUsesExistingLocalConversionTool() = runBlocking {
        val config = AppConfig(files = false, contacts = false, aiProvider = "gemini")
        val query = "¿Cuántos metros son 2 km?"
        val resolution = UniversalIntentEngine.resolve(query, config, day, zone)
        assertEquals(PrimaryIntentKind.LOCAL_TOOL, resolution.primaryIntent)
        assertEquals(listOf("convert_units"), resolution.tools)
        assertEquals(RemoteAiEscalation.NONE, resolution.aiEscalation)
        val rows = engine().search(query, config).toList().last()
        assertEquals("2000 metros", rows.first().title)
        assertTrue(rows.first().action is Action.Copy)
        assertFalse(rows.any { it.action is Action.Ask })
    }

    @Test
    fun phase2_resolvesAllTenNaturalQueriesWithEntitiesCapabilitiesPermissionsAndConfirmation() {
        val config = AppConfig(aiProvider = "gemini")

        // 1. «Abrí Spotify»
        val openSpotify = UniversalIntentEngine.resolve("Abrí Spotify", config, day, zone)
        assertEquals(PrimaryIntentKind.OPEN_APP, openSpotify.primaryIntent)
        assertTrue(openSpotify.entities.any { it.type == EntityType.APP_NAME && it.normalized == "spotify" })
        assertTrue(RequiredCapability.PACKAGE_RESOLVE in openSpotify.capabilities)
        assertEquals(RemoteAiEscalation.NONE, openSpotify.aiEscalation)
        assertFalse(openSpotify.requiresConfirmation)

        // 2. «¿Cuántas Libertadores ganó Boca?»
        val boca = UniversalIntentEngine.resolve("¿Cuántas Libertadores ganó Boca?", config, day, zone)
        assertEquals(PrimaryIntentKind.KNOWLEDGE_AI, boca.primaryIntent)
        assertEquals(RemoteAiEscalation.EXPLICIT_PRIMARY, boca.aiEscalation)
        assertEquals("Responder con Gemini", boca.presentation.primaryTitle)

        // 3. «Buscá el PDF de anatomía que abrí ayer»
        val pdfAnatomia = UniversalIntentEngine.resolve("Buscá el PDF de anatomía que abrí ayer", config, day, zone)
        assertEquals(PrimaryIntentKind.FILE_SEARCH, pdfAnatomia.primaryIntent)
        assertNotNull(pdfAnatomia.fileConstraint)
        assertEquals("pdf", pdfAnatomia.fileConstraint!!.kind)
        assertEquals("anatomia", pdfAnatomia.fileConstraint!!.terms)
        assertEquals("opened", pdfAnatomia.fileConstraint!!.dateField)
        assertNotNull(pdfAnatomia.fileConstraint!!.unsupportedDateReason)
        assertFalse(pdfAnatomia.fileConstraint!!.query.contains("modified-"))
        assertEquals(Instant.parse("2026-10-14T03:00:00Z").toEpochMilli(), pdfAnatomia.fileConstraint!!.start)
        assertTrue(RequiredPermission.READ_EXTERNAL_STORAGE_OR_SAF in pdfAnatomia.permissions)

        // 4. «Explicame este documento»
        val explainDoc = UniversalIntentEngine.resolve("Explicame este documento", config, day, zone)
        assertEquals(PrimaryIntentKind.CONTEXT_AI, explainDoc.primaryIntent)
        assertEquals(RemoteAiEscalation.EXPLICIT_WITH_CONTEXT_CONFIRMATION, explainDoc.aiEscalation)
        assertTrue(explainDoc.requiresConfirmation)

        // 5. «Abrí Spotify y bajá el volumen al 40%»
        val compoundAppVol = UniversalIntentEngine.resolve("Abrí Spotify y bajá el volumen al 40%", config, day, zone)
        assertEquals(PrimaryIntentKind.COMPOUND_ACTION, compoundAppVol.primaryIntent)
        assertNotNull(compoundAppVol.localPlan)
        assertEquals(listOf(PlanStep.Open("spotify"), PlanStep.Volume(40)), compoundAppVol.localPlan!!.steps)
        assertEquals(listOf("open_app", "set_volume"), compoundAppVol.tools)
        assertTrue(compoundAppVol.requiresConfirmation)

        // 6. «Mandale un WhatsApp a Juan»
        val whatsappJuan = UniversalIntentEngine.resolve("Mandale un WhatsApp a Juan", config, day, zone)
        assertEquals(PrimaryIntentKind.COMMUNICATION, whatsappJuan.primaryIntent)
        assertTrue(whatsappJuan.entities.any { it.type == EntityType.CONTACT_NAME && it.value == "Juan" })
        assertTrue(RequiredPermission.READ_CONTACTS in whatsappJuan.permissions)
        assertTrue(whatsappJuan.requiresConfirmation)

        // 7. «Cómo va River»
        val sportsRiver = UniversalIntentEngine.resolve("Cómo va River", config, day, zone)
        assertEquals(PrimaryIntentKind.SPORTS, sportsRiver.primaryIntent)
        assertTrue(sportsRiver.entities.any { it.type == EntityType.SPORT_ENTITY && it.value == "River" })
        assertEquals(RemoteAiEscalation.NONE, sportsRiver.aiEscalation)

        // 8. «Va a llover mañana en Mar del Plata»
        val weatherMdp = UniversalIntentEngine.resolve("Va a llover mañana en Mar del Plata", config, day, zone)
        assertEquals(PrimaryIntentKind.WEATHER, weatherMdp.primaryIntent)
        assertTrue(weatherMdp.entities.any { it.type == EntityType.LOCATION && it.value == "Mar del Plata" })
        assertTrue(weatherMdp.entities.any { it.type == EntityType.TIME_RANGE && it.value == "tomorrow" })
        val weatherRoot = RootActions.results(context, "Va a llover mañana en Mar del Plata", config)
        assertTrue(weatherRoot.any { it.action == Action.SystemPanel("weather-native", "tomorrow:Mar del Plata") })

        // 9. «Quiero estudiar sin distracciones»
        val study = UniversalIntentEngine.resolve("Quiero estudiar sin distracciones", config, day, zone)
        assertEquals(PrimaryIntentKind.FOCUS_STUDY, study.primaryIntent)
        assertTrue(study.requiresConfirmation)

        // 10. «Mostrame mis archivos PDF»
        val myPdfs = UniversalIntentEngine.resolve("Mostrame mis archivos PDF", config, day, zone)
        assertEquals(PrimaryIntentKind.FILE_SEARCH, myPdfs.primaryIntent)
        assertEquals("pdf", myPdfs.fileConstraint!!.kind)
        assertEquals("", myPdfs.fileConstraint!!.terms)
        assertEquals("kind:pdf", myPdfs.fileConstraint!!.query)
    }

    @Test
    fun phase4_contextualSuggestionsEnforceMaxOneAppDecayAndZeroAlphabeticalFiller() {
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        listOf(
            "com.example.ajedrez" to "Ajedrez",
            "com.spotify.music" to "Spotify",
            "com.whatsapp" to "WhatsApp",
        ).forEach { (pkg, label) ->
            val appInfo = ApplicationInfo().apply {
                packageName = pkg
                nonLocalizedLabel = label
                if (pkg == "com.spotify.music") category = ApplicationInfo.CATEGORY_AUDIO
            }
            val actInfo = ActivityInfo().apply {
                packageName = pkg
                name = "$pkg.Main"
                nonLocalizedLabel = label
                applicationInfo = appInfo
            }
            Shadows.shadowOf(context.packageManager).addResolveInfoForIntent(launcher, ResolveInfo().apply { activityInfo = actInfo })
        }

        val now = 1_760_570_000_000L
        // 1. Fresh state without habits or favorites returns clean empty screen
        assertTrue(HomeSuggestions.results(context, AppConfig(files = false), false, false, now).isEmpty())

        // 2. Multiple recent apps still yield at most 1 app suggestion, with continuity subtitle for Spotify
        val withRecents = AppConfig(
            files = false,
            learning = true,
            recent = listOf("com.spotify.music/com.spotify.music.Main", "com.whatsapp/com.whatsapp.Main"),
            actionRecent = listOf("volumen al 40%"),
        )
        val rows = HomeSuggestions.results(context, withRecents, false, false, now)
        val appRows = rows.filter { it.id.startsWith("suggest:app:") }
        assertEquals(1, appRows.size)
        assertEquals("Spotify", appRows.single().title)
        assertEquals("Aplicación multimedia reciente", appRows.single().subtitle)
        assertFalse(appRows.single().subtitle.contains("En reproducción"))

        // 3. Stale usage older than 14 days expires and is not suggested
        val staleTime = now - 20L * 86_400_000L
        val staleUsage = """{"com.whatsapp/com.whatsapp.Main":{"count":25,"time":$staleTime}}"""
        val expiredRows = HomeSuggestions.results(context, AppConfig(files = false, usageData = staleUsage), false, false, now)
        assertTrue(expiredRows.isEmpty())
    }

    @Test
    fun phase5_buscarDoesNotProduceIdenticalBuscarContenidoRowsOrLeakIntoFilesScope() = runBlocking {
        // Bare verb "buscar" produces zero noisy extension commands
        assertTrue(ExtensionRegistry.searchCommands(context, "buscar", AppConfig()).isEmpty())

        // Topic queries qualify the title with the extension name ("Buscar en Spotify", not generic "Buscar contenido")
        val spotifyCommands = ExtensionRegistry.searchCommands(context, "spotify", AppConfig())
        assertTrue(spotifyCommands.any { it.title == "Buscar en Spotify" })
        assertTrue(spotifyCommands.none { it.title == "Buscar contenido" })

        // Visual & action deduplication removes duplicate rows
        val dup1 = Result("id1", "Buscar contenido", "Sub", "Aplicaciones", 1000, Action.ComposeSearch("spotify", "com.spotify.music"))
        val dup2 = Result("id2", "Buscar contenido", "Sub", "Aplicaciones", 900, Action.ComposeSearch("youtube", "com.google.android.youtube"))
        assertEquals(1, StableResults.unique(listOf(dup1, dup2)).size)
    }

    @Test
    fun phase6_compoundDocumentAndAiPlanExtractsTargetChapterLocally() {
        val query = "Buscá el PDF de rehabilitación vestibular y explicame el capítulo 3"
        val plan = UniversalIntentEngine.parseDocumentAiCompound(query, day, zone)
        assertNotNull(plan)
        assertEquals("pdf", plan!!.fileKind)
        assertEquals("rehabilitacion vestibular", plan.searchTerms)
        assertEquals("explicame el capítulo 3", plan.aiInstruction)
        assertEquals("capítulo 3", plan.sectionTarget.lowercase())

        val samplePdfText = """
            Capítulo 1: Anatomía del sistema vestibular
            El laberinto membranoso incluye los conductos semicirculares.

            Capítulo 2: Fisiología del reflejo vestibuloocular
            El VOR estabiliza la mirada durante rotaciones cefálicas.

            Capítulo 3: Maniobras de reposicionamiento canalicular
            La maniobra de Epley y la maniobra de Semont se emplean en el VPPB del conducto posterior.
            Los ejercicios de habituación de Cawthorne-Cooksey complementan la rehabilitación.
        """.trimIndent()

        val section = DocumentSectionLocator.locate(samplePdfText, plan.sectionTarget)
        assertTrue(section.matchedSection)
        assertTrue(section.heading.contains("Capítulo 3"))
        assertTrue(section.excerpt.contains("maniobra de Epley"))
        assertFalse(section.excerpt.contains("Capítulo 1"))
    }

    @Test
    fun phase7_expandedCompoundAndContextualAiPreserveExplicitConsentAndPrivateOrigin() {
        val compoundExtract = UniversalIntentEngine.parseDocumentAiCompound(
            "Buscá el PDF de farmacología y extraé el punto 2",
            day,
            zone,
        )
        assertNotNull(compoundExtract)
        assertEquals("pdf", compoundExtract!!.fileKind)
        assertEquals("farmacologia", compoundExtract.searchTerms)
        assertEquals("punto 2", compoundExtract.sectionTarget.lowercase())

        for (query in listOf(
            "Corregí este texto",
            "Analizá este documento",
            "Resumí este PDF",
            "Compará estos 2 documentos",
        )) {
            val res = UniversalIntentEngine.resolve(query, AppConfig(aiProvider = "gemini"), day, zone)
            assertEquals(PrimaryIntentKind.CONTEXT_AI, res.primaryIntent)
            assertEquals(RemoteAiEscalation.EXPLICIT_WITH_CONTEXT_CONFIRMATION, res.aiEscalation)
            assertTrue(res.requiresConfirmation)
            assertTrue(RequiredPermission.AI_CONTEXT_CONSENT in res.permissions)
        }
    }
}
