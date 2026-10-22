package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.data.Config
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config as RoboConfig
import org.robolectric.annotation.GraphicsMode

/** UI-contract tests for the quick actions/settings entry points. */
@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RaycastQuickMenuComposeTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()

    @Test fun permissionsShortcutOpensUnifiedSection() {
        var destination:String?=null
        ui.setContent {
            SpotlightTheme("dark") {
                GlassQuickMenu(
                    Config(),{},{group->destination=group},{}
                )
            }
        }
        ui.onNodeWithText("Permisos").assertExists().performClick()
        ui.runOnIdle { assertEquals("Permisos y accesos",destination) }
    }

    @Test fun essentialMenuHasFourRealSettingsDestinations() {
        var destination:String?=null
        ui.setContent{SpotlightTheme("dark"){GlassQuickMenu(Config(),{},{destination=it},{})}}
        listOf("Ajustes" to "General","Apariencia" to "Apariencia","Permisos" to "Permisos y accesos","Atajos" to "Atajos").forEach{(label,group)->
            ui.onNodeWithText(label).assertIsDisplayed().performClick()
            ui.runOnIdle{assertEquals(group,destination)}
        }
        ui.onAllNodes(hasClickAction()).assertCountEquals(4)
        ui.onNodeWithText("IA").assertDoesNotExist()
        ui.onNodeWithText("Privado").assertDoesNotExist()
    }
}
