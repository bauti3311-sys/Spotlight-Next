package dev.bautista.spotlight.ui

import android.content.pm.*
import android.graphics.*
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Next100SystemVisualTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun capture(name:String){ui.waitForIdle();ui.runOnIdle{val view=ui.activity.window.decorView;val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888);view.draw(Canvas(bitmap));File("build/100-ui/$name.png").also{it.parentFile?.mkdirs()}.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}}
    @Test fun appInspectorShowsPublicMetadataWithoutShizuku(){
        val p=PackageInfo().apply{packageName="fixture.music";versionName="2.1";applicationInfo=ApplicationInfo().apply{packageName="fixture.music";nonLocalizedLabel="Música de prueba"};requestedPermissions=arrayOf("android.permission.INTERNET");requestedPermissionsFlags=intArrayOf(PackageInfo.REQUESTED_PERMISSION_GRANTED);activities=arrayOf(ActivityInfo().apply{name="fixture.music.Main";exported=true})}
        Shadows.shadowOf(app.packageManager).installPackage(p)
        ui.setContent{SpotlightTheme{Surface{ApplicationInspector(app,"fixture.music")}}}
        ui.waitUntil(15000){ui.onAllNodesWithText("Música de prueba").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Permisos declarados · 1").performClick();ui.onNodeWithText("INTERNET · concedido").assertExists();ui.onNodeWithText("Componentes públicos · 1").performClick();ui.onNodeWithText("Actividad · fixture.music.Main").assertExists();ui.onNodeWithText("Detener aplicación · Shizuku").assertDoesNotExist();capture("application-inspector-tablet")
    }
    @Test @Config(qualifiers="w360dp-h800dp") fun standardControlsRemainUsableWithoutPrivilege(){ui.setContent{SpotlightTheme{Surface{AdvancedPanel(app,"controls")}}};ui.onNodeWithText("No instalado").assertExists();ui.onNodeWithText("Autorizar Spotlight").assertIsNotEnabled();capture("controls-standard-phone")}
}
