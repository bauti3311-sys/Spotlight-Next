package dev.bautista.spotlight.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.MainActivity
import dev.bautista.spotlight.SpotlightApp
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsWorkspaceTest {
 @get:Rule val ui=createAndroidComposeRule<MainActivity>()
 private fun ready(){ui.waitUntil(15000){ui.onAllNodesWithTag("settings-search").fetchSemanticsNodes().isNotEmpty()}}
 private fun capture(name:String){ui.waitForIdle();ui.onRoot().captureRoboImage("build/settings-integral/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))}
 @Test fun tabletHasSevenAreasAndIndependentDetailsPane(){
  ready();ui.onNodeWithText("Avanzado").assertExists();ui.onNodeWithText("Integraciones").performClick()
  ui.onNodeWithText("Tasker · automatización externa").assertDoesNotExist()
  ui.onNodeWithText("Rutinas y automatizaciones").assertDoesNotExist()
  ui.onNodeWithText("Centro de comandos y perfiles").assertDoesNotExist()
  capture("tablet-integrations")
 }
 @Test fun appearanceHasOneWidthAndResetRequiresConfirmation(){
  ready();ui.onNodeWithText("Apariencia").performClick()
  ui.onAllNodesWithTag("window-width-slider").assertCountEquals(1)
  ui.onNodeWithText("Ancho horizontal").assertDoesNotExist()
  capture("tablet-appearance")
  ui.onNodeWithText("Restablecer apariencia").performScrollTo().performClick()
  ui.onNodeWithText("Confirmar").assertExists();ui.onNodeWithText("Cancelar").performClick()
  ui.onNodeWithText("Confirmar").assertDoesNotExist()
 }
 @Test fun globalSearchFindsGeminiAndReturnsToCategory(){
  ready();ui.onNodeWithTag("settings-search").performTextInput("gemini clave")
  ui.onNodeWithText("Modelo").assertExists()
  ui.onNodeWithText("Eliminar clave").assertExists()
  ui.onNodeWithContentDescription("Limpiar búsqueda").performClick()
  ui.onNodeWithText("Resultados de ajustes").assertDoesNotExist()
 }
 @Test @Config(qualifiers="w360dp-h800dp") fun phoneCanChooseAreaAndScrollAllControls(){
  ready();ui.onNodeWithText("Cambiar").performClick();ui.onNodeWithText("Apariencia").performClick()
  ui.onAllNodesWithTag("window-width-slider").assertCountEquals(1)
  ui.onNodeWithText("Restablecer apariencia").performScrollTo().assertIsDisplayed()
  capture("phone-appearance")
 }
}
