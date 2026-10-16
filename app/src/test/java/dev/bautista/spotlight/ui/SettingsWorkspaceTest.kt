package dev.bautista.spotlight.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.input.key.Key
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.MainActivity
import dev.bautista.spotlight.SpotlightApp
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
class SettingsWorkspaceTest {
 @get:Rule val ui=createAndroidComposeRule<MainActivity>()
 private fun ready(){ui.waitUntil(15000){ui.onAllNodesWithTag("settings-search").fetchSemanticsNodes().isNotEmpty()}}
 private fun capture(name:String){ui.waitForIdle();ui.onRoot().captureRoboImage("build/settings-integral/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))}
 @Test fun tabletHasSevenAreasAndIndependentDetailsPane(){
  ready();ui.onNodeWithText("Avanzado").assertExists();ui.onNodeWithText("Integraciones").performClick()
  val rail=ui.onNodeWithText("Avanzado").fetchSemanticsNode().boundsInRoot
  ui.onNodeWithText("Shizuku · integración avanzada opcional").performScrollTo()
  Assert.assertEquals(rail,ui.onNodeWithText("Avanzado").fetchSemanticsNode().boundsInRoot)
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
  ui.onNodeWithTag("settings-search").performTextInput("ancho")
  ui.onAllNodesWithTag("window-width-slider").assertCountEquals(1)
  ui.onNodeWithTag("settings-search").performKeyInput{pressKey(Key.Escape)}
  ui.onNodeWithText("Resultados de ajustes").assertDoesNotExist()
 }
 @Test fun appearanceCategorySurvivesActivityRecreation(){
  ready();ui.onNodeWithText("Apariencia").performClick()
  ui.activityRule.scenario.recreate();ready()
  ui.onAllNodesWithTag("window-width-slider").assertCountEquals(1)
 }
 @Test fun darkCapabilitiesReflectUnavailablePermissions(){
  ready();val app=ui.activity.application as SpotlightApp
  runBlocking{val current=app.preferences.config.first();app.preferences.updateAppearance(current.copy(theme="dark",preset="glass"))}
  ui.onNodeWithText("Integraciones").performClick()
  ui.onNodeWithText("Integraciones · capacidades disponibles").performScrollTo().performClick()
  ui.waitUntil(15000){ui.onAllNodesWithText("Buscar contactos").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithText("Buscar contactos").assertExists()
  capture("tablet-obsidian-capabilities")
 }
 @Test @Config(qualifiers="w360dp-h800dp") fun phoneCanChooseAreaAndScrollAllControls(){
  ready();ui.onNodeWithText("Cambiar").performClick();ui.onNodeWithText("Apariencia").performClick()
  ui.onAllNodesWithTag("window-width-slider").assertCountEquals(1)
  ui.onNodeWithText("Restablecer apariencia").performScrollTo().assertIsDisplayed()
  capture("phone-appearance")
 }
}
