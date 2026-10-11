package dev.bautista.spotlight.ui

import android.graphics.*
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Next110PanelTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun capture(name:String){ui.waitForIdle();ui.runOnIdle{val view=ui.activity.window.decorView;val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888);view.draw(Canvas(bitmap));File("build/110-ui/$name.png").also{it.parentFile?.mkdirs()}.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}}
    @Test fun catalogReturnsWithoutLosingQuery(){ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"catálogo",700f)};ui.waitUntil(15000){ui.onAllNodesWithText("catálogo").fetchSemanticsNodes().size>=2};ui.onNode(hasText("catálogo") and !hasSetTextAction()).performClick();ui.onNodeWithText("Centro de comandos").assertExists();capture("catalog-tablet");ui.onAllNodesWithText("Volver").onFirst().performClick();ui.onNode(hasSetTextAction()).assertTextContains("catálogo")}
    @Test @Config(qualifiers="w360dp-h800dp") fun catalogWorksOnPhone(){ui.setContent{SpotlightTheme{Surface{NextPanel(app,"catalog","") {}}}};ui.onNodeWithText("Buscar acciones").performTextInput("Wi-Fi");ui.onNodeWithText("Ajustes de Wi-Fi").assertExists();ui.onNodeWithText("Abrir").assertIsDisplayed();capture("catalog-phone")}
    @Test @Config(qualifiers="w360dp-h800dp") fun taskerMissingIsHonest(){ui.setContent{SpotlightTheme{Surface{NextPanel(app,"tasker","") {}}}};ui.waitUntil(15000){ui.onAllNodesWithText("Tasker no está instalado. Las funciones locales no lo necesitan.").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Confirmar ejecución").assertDoesNotExist();capture("tasker-missing-phone")}
    @Test fun profileRequiresExplicitExecution(){ui.setContent{SpotlightTheme{Surface{NextPanel(app,"profiles","estudio") {}}}};ui.onNodeWithText("Modo estudio").assertExists();ui.onNodeWithText("Ejecutar").assertExists();capture("study-review-tablet")}
    @Test @Config(qualifiers="w360dp-h800dp") fun cachedWeatherPanelFitsPhoneWithoutInternet(){
        val fixture=org.json.JSONObject().put("current",org.json.JSONObject().put("temperature_2m",18).put("weather_code",0).put("relative_humidity_2m",70).put("apparent_temperature",17).put("wind_speed_10m",12).put("wind_gusts_10m",20).put("precipitation",0)).put("timezone","America/Argentina/Buenos_Aires")
        app.getSharedPreferences("native_provider_cache_v1",0).edit().putString("weather:-38.0023:-57.5575",org.json.JSONObject().put("saved",System.currentTimeMillis()).put("body",fixture).toString()).commit()
        ui.setContent{SpotlightTheme{Surface{NextPanel(app,"weather-native","Mar del Plata") {}}}}
        ui.waitUntil(15000){ui.onAllNodesWithText("18 °C").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("18 °C").performScrollTo().assertIsDisplayed();capture("weather-cached-phone")
    }
    @Test fun missingSportsScoreIsVisibleWithoutFakeLiveMinute(){
        val fixture=org.json.JSONObject().put("events",org.json.JSONArray().put(org.json.JSONObject().put("idEvent","fixture-1").put("strEvent","Equipo A vs Equipo B").put("strLeague","Liga de prueba")))
        app.getSharedPreferences("native_provider_cache_v1",0).edit().putString("sports:Soccer:${java.time.LocalDate.now()}",org.json.JSONObject().put("saved",System.currentTimeMillis()).put("body",fixture).toString()).commit()
        ui.setContent{SpotlightTheme{Surface{NextPanel(app,"sports-native","") {}}}}
        ui.waitUntil(15000){ui.onAllNodesWithText("Marcador no informado").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Marcador no informado").performScrollTo().assertIsDisplayed();ui.onNodeWithText("Estado no informado").assertExists();capture("sports-cached-tablet")
    }

}
