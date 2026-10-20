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

    @Test fun appearanceAndSearchShortcutsStayFunctional() {
        var changed:Config?=null
        var mode:String?=null
        ui.setContent {
            SpotlightTheme("dark") {
                GlassQuickMenu(
                    Config(),{changed=it},{},{mode=it}
                )
            }
        }
        ui.onNodeWithText("Tema · Automático").performClick()
        ui.runOnIdle { assertEquals("light",changed?.theme) }
        ui.onNodeWithContentDescription("Aplicaciones · Ctrl/⌘+1").performClick()
        ui.runOnIdle { assertEquals("Apps",mode) }
    }
}
