package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReferenceWindowContractTest {
 @get:Rule val ui=createComposeRule()
 private fun capture(name:String)=ui.onRoot().captureRoboImage("build/reference-window/$name.png",
  roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))

 @Test fun trailingDockKeepsIndependentRealActionsAndOptions() {
  var opens=0;var actions=0;var options=0
  ui.setContent { SpotlightTheme("dark") { Box(Modifier.width(900.dp)) {
   GlassContextFooter(false,true,"Abrir",{opens++},{actions++},onOptions={options++})
  } } }
  val dock=ui.onNodeWithTag("command-dock-actions").getUnclippedBoundsInRoot()
  val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
  assertTrue(dock.left>footer.left+(footer.right-footer.left)/2)
  ui.onNodeWithText("Abrir").performClick()
  ui.onNodeWithText("Acciones").performClick()
  ui.onNodeWithContentDescription("Opciones de Spotlight").performClick()
  ui.runOnIdle{assertEquals(1,opens);assertEquals(1,actions);assertEquals(1,options)}
  ui.onNodeWithTag("command-dock-actions").assertHeightIsAtLeast(48.dp)
  capture("dock-tablet")
 }
 @Test @Config(qualifiers="w360dp-h800dp") fun narrowDockDoesNotClipOrDuplicatePrimaryAction() {
  ui.setContent { SpotlightTheme("dark") { Box(Modifier.width(328.dp)) {
   GlassContextFooter(false,true,"Abrir aplicación",{},{},onOptions={},onExpand={})
  } } }
  ui.onAllNodesWithText("Abrir aplicación").assertCountEquals(1)
  ui.onNodeWithText("Acciones").assertIsDisplayed()
  val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
  val dock=ui.onNodeWithTag("command-dock-actions").getUnclippedBoundsInRoot()
  assertTrue(dock.right<=footer.right);assertTrue(dock.left>=footer.left)
  capture("dock-phone")
 }
 @Test fun fileLikeRowsAlignTitleAndLocationWithoutSecondLine() {
  ui.setContent { SpotlightTheme("dark") { Column(Modifier.width(900.dp)) {
   ResultRow(Result("fixture:invoice","Invoice-0001.pdf","Descargas","Archivos",1,
    Action.Copy("fixture"),mime="application/pdf"),true,false,{},{},{})
  } } }
  val title=ui.onNodeWithText("Invoice-0001.pdf",useUnmergedTree=true).getUnclippedBoundsInRoot()
  val location=ui.onNodeWithText("Descargas",useUnmergedTree=true).getUnclippedBoundsInRoot()
  assertEquals(title.top.value,location.top.value,2f)
  assertTrue(location.left>=title.right)
  ui.onNodeWithText("PDF").assertIsDisplayed()
  ui.onNodeWithTag("result:fixture:invoice").assertHeightIsAtLeast(48.dp)
  capture("file-row")
 }
 @Test fun settingsSearchLivesInRailAndNavigationKeepsRealState() {
  var selected by mutableStateOf("General")
  ui.setContent { SpotlightTheme("dark") { Box(Modifier.width(1000.dp).height(720.dp)) {
   SettingsLayout(selected,{selected=it}) { Text("Contenido de $selected") }
  } } }
  val search=ui.onNodeWithTag("settings-search").getUnclippedBoundsInRoot()
  val details=ui.onNodeWithTag("settings-details").getUnclippedBoundsInRoot()
  assertTrue(search.right<details.left)
  ui.onNodeWithTag("settings-area:Integraciones").performClick()
  ui.onNodeWithText("Contenido de Integraciones").assertIsDisplayed()
  ui.onNodeWithTag("settings-search").performTextInput("Spotify")
  ui.onNodeWithText("Resultados de ajustes").assertIsDisplayed()
  ui.onNodeWithContentDescription("Limpiar búsqueda").performClick()
  ui.onNodeWithText("Contenido de Integraciones").assertIsDisplayed()
  capture("settings-tablet")
 }
 @Test fun filePreviewHasMoreSpaceThanItsNavigationList() {
  ui.setContent { SpotlightTheme("dark") { Box(Modifier.width(1000.dp)) {
   ContextSplitPanel(500.dp,{Text("Lista")},{Text("Vista previa")},initialFraction=.42f)
  } } }
  val list=ui.onNodeWithTag("results-pane").getUnclippedBoundsInRoot()
  val preview=ui.onNodeWithTag("context-inspector").getUnclippedBoundsInRoot()
  assertTrue(list.right-list.left<preview.right-preview.left)
 }
 @Test fun documentMetadataUsesRealFieldsAndDoesNotInventMissingDates() {
  ui.setContent { SpotlightTheme("dark") { Column(Modifier.width(500.dp)) {
   QuickLookMetadata(Result("fixture:metadata","Invoice-0001.pdf","","Archivos",1,
    Action.Copy("fixture"),mime="application/pdf",sizeBytes=1024L,parentUri="content://fixture/document/primary%3ADownload"))
  } } }
  for(label in listOf("Nombre","Ubicación","Tipo","Tamaño")) {
   ui.onNodeWithTag("file-metadata:$label").assertIsDisplayed()
   val bounds=ui.onNodeWithTag("file-metadata:$label").getUnclippedBoundsInRoot()
   assertTrue("A single-line metadata row must not stretch: $label",bounds.bottom-bounds.top<=56.dp)
   ui.onNodeWithText(label,useUnmergedTree=true).assertIsDisplayed()
  }
  ui.onNodeWithText("Invoice-0001.pdf").assertIsDisplayed()
  ui.onNodeWithText("Download").assertIsDisplayed()
  ui.onNodeWithText("application/pdf").assertIsDisplayed()
  ui.onNodeWithTag("file-metadata:Tamaño").assertIsDisplayed()
  ui.onNodeWithTag("file-metadata:Modificado").assertDoesNotExist()
  capture("file-metadata")
 }
 @Test fun optionsKeepSavedAndDictationExplicitWithoutChangingSearch() {
  var saved=0;var dictation=0
  ui.setContent { SpotlightTheme("dark") {
   SpotlightBar("Invoice",{},"Todo",{},{},{},remember{androidx.compose.ui.focus.FocusRequester()},1f,false,
    onSaved={saved++},onDictate={dictation++})
  } }
  ui.onNodeWithText("Abrir Guardados").assertDoesNotExist()
  ui.onNodeWithContentDescription("Opciones de Spotlight").performClick()
  ui.onNodeWithText("Abrir Guardados").performClick()
  ui.runOnIdle{assertEquals(1,saved);assertEquals(0,dictation)}
  ui.onNode(hasSetTextAction()).assertTextEquals("Invoice")
  ui.onNodeWithContentDescription("Opciones de Spotlight").performClick()
  ui.onNodeWithText("Dictar consulta").performClick()
  ui.runOnIdle{assertEquals(1,saved);assertEquals(1,dictation)}
  ui.onAllNodes(isPopup()).assertCountEquals(0)
  ui.onNode(hasSetTextAction()).assertTextEquals("Invoice")
 }
 @Test fun longDocumentNameAtLargeTextKeepsItsLabelAndWrapsInsideTheRow() {
  val name="Factura de servicios del departamento correspondiente a octubre de 2026.pdf"
  ui.setContent { CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides
   androidx.compose.ui.unit.Density(androidx.compose.ui.platform.LocalDensity.current.density,1.5f)) {
   SpotlightTheme("dark") { Column(Modifier.width(320.dp)) {
    QuickLookMetadata(Result("fixture:long-metadata",name,"","Archivos",1,Action.Copy("fixture"),
     mime="application/pdf",sizeBytes=2048L))
   } }
  } }
  ui.onNodeWithText("Nombre",useUnmergedTree=true).assertIsDisplayed()
  ui.onNodeWithText(name,useUnmergedTree=true).assertIsDisplayed()
  val row=ui.onNodeWithTag("file-metadata:Nombre").getUnclippedBoundsInRoot()
  val value=ui.onNodeWithText(name,useUnmergedTree=true).getUnclippedBoundsInRoot()
  assertTrue(value.right<=row.right);assertTrue(value.bottom<=row.bottom)
  capture("file-metadata-large-text")
 }
}
