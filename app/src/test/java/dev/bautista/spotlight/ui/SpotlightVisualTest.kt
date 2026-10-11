package dev.bautista.spotlight.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SpotlightVisualTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private fun capture(theme:String) {
        ui.setContent {
            SpotlightTheme(theme) {
                Box(Modifier.width(700.dp).padding(24.dp)) {
                    SpotlightBar("youtube",{},"Todo",{},{},{},remember {FocusRequester()},.96f,false,suggestion="youtube música para estudiar",animations=false)
                }
            }
        }
        ui.onNodeWithContentDescription("Aplicaciones · Ctrl/⌘+1").assertIsDisplayed()
        val output=File("build/spotlight-$theme.png");output.parentFile?.mkdirs()
        ui.runOnIdle {
            val view=ui.activity.window.decorView
            val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            output.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
            bitmap.recycle()
        }
    }
    @Test fun lightNativeBar(){capture("light")}
    @Test fun darkNativeBar(){capture("dark")}
}
