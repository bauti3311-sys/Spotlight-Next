package dev.bautista.spotlight.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class ApplicationInspectorAccessibilityTest {
    @get:Rule val ui=createComposeRule()

    @Test fun secondaryActionsExposeButtonRolesAndOnlyInvokeTheirOwnCallback() {
        var opens=0
        var favorites=0
        var info=0
        var panels=0
        val media=mutableListOf<String>()
        ui.setContent {
            MaterialTheme {
                ApplicationInspector(
                    item=Result("fixture:app","Fixture",subtitle="",category="Aplicaciones",score=1,
                        action=Action.Copy("fixture")),
                    packageName=null,favorite=false,playing=false,mediaCommands=listOf("play","next"),
                    onOpen={opens++},onFavorite={favorites++},onInfo={info++},
                    onActions={panels++},onMedia={media+=it},
                )
            }
        }
        val button=SemanticsMatcher.expectValue(SemanticsProperties.Role,Role.Button)
        for(label in listOf("Ver información de la app","Añadir favorita","Reproducir",
            "Siguiente canción","Ver acciones contextuales")) {
            ui.onNode(hasText(label) and button).assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        }
        ui.onNodeWithText("Ver información de la app").performClick()
        ui.onNodeWithText("Añadir favorita").performClick()
        ui.onNodeWithText("Reproducir").performClick()
        ui.onNodeWithText("Siguiente canción").performClick()
        ui.onNodeWithText("Ver acciones contextuales").performClick()
        ui.runOnIdle {
            assertEquals(0,opens)
            assertEquals(1,favorites)
            assertEquals(1,info)
            assertEquals(1,panels)
            assertEquals(listOf("play","next"),media)
        }
    }
}
