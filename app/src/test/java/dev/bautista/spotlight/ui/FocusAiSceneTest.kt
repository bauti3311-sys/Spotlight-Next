package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config as AppConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.concurrent.atomic.AtomicInteger

/** Production stream/Markdown and callbacks, entirely intercepted in memory; no HTTP connection. */
@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FocusAiSceneTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private fun scene(width:Int,theme:String) {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        val calls=AtomicInteger(0)
        val text="## Respuesta local de prueba\n\nEl reflejo vestibuloocular estabiliza la mirada al mover la cabeza.\n\n- Selecciona una fuente autorizada.\n- Revisa el contexto antes de compartirlo.\n\n**Fixture de interfaz: no es asesoramiento clínico ni respuesta de un proveedor real.**"
        val payload=org.json.JSONObject().put("candidates",org.json.JSONArray().put(org.json.JSONObject().put("content",org.json.JSONObject().put("parts",org.json.JSONArray().put(org.json.JSONObject().put("text",text))))))
        val client=DirectAi(object:AiCredentials {
            override fun read(provider:String)="fixture-not-a-real-key"
            override fun contains(provider:String)=true
        },OkHttpClient.Builder().addInterceptor { chain ->
            calls.incrementAndGet()
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("Local fixture")
                .body(("data: $payload\n\n").toResponseBody("text/event-stream".toMediaType())).build()
        }.build())
        val phase="focus"
        var premium by mutableStateOf(phase=="after")
        ui.setContent{CompositionLocalProvider(LocalPremiumReview provides premium){SpotlightTheme(theme){Surface{Box(Modifier.width(width.dp)){
            SpotlightPanel(app,AppConfig(theme=theme,focusInterface=true,preset="glass",files=false,online=false,contacts=false,animations=false,learning=false,
                aiProvider="gemini",aiModel="fixture-model"),{},"Explicame el reflejo vestibuloocular",700f,aiClient=client)
        }}}}}
        ui.waitUntil(15000){ui.onAllNodesWithText("Responder con Gemini").fetchSemanticsNodes().isNotEmpty()}
        assertEquals(0,calls.get())
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).performKeyInput{pressKey(Key.Enter)}
        ui.waitUntil(15000){ui.onAllNodesWithText("Respuesta local de prueba").fetchSemanticsNodes().isNotEmpty()}
        ui.waitForIdle();assertEquals(1,calls.get())
        val profile="${if(width>600)"tablet" else "phone"}-$theme"
        for(mode in if(phase=="paired")listOf("before","after") else listOf(phase)) {
            ui.runOnIdle{premium=mode=="after"};ui.waitForIdle()
            ui.onNodeWithTag("spotlight-root").captureRoboImage("build/focus-content/$mode/$profile-ai-result.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
            assertEquals(1,calls.get())
        }
        ui.onNodeWithText("Copiar",useUnmergedTree=true).performScrollTo().performClick()
        val copied=app.getSystemService(android.content.ClipboardManager::class.java).primaryClip?.getItemAt(0)?.text.toString()
        assertTrue(copied.contains("Respuesta local de prueba"));assertEquals(1,calls.get())
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun tabletDark()=scene(960,"dark")
    @Test fun tabletLight()=scene(960,"light")
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneDark()=scene(360,"dark")
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneLight()=scene(360,"light")
}
