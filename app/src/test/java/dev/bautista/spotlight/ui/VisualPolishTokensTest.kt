package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w600dp-h500dp")
@OptIn(ExperimentalTestApi::class)
class VisualPolishTokensTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private fun radius(shape:androidx.compose.ui.graphics.Shape)=
  (shape.createOutline(Size(200f,60f),LayoutDirection.Ltr,Density(1f)) as Outline.Rounded).roundRect.topLeftCornerRadius.x
 private fun fields(theme:String) {
  var oldControl by mutableStateOf(true)
  var value by mutableStateOf(TextFieldValue("Texto de prueba"))
  var actualRadius=0f;var title=0.sp;var chipRadius=0f;var keptSelection=false
  ui.setContent {SpotlightTheme(theme,AppConfig(focusInterface=true,animations=false)) {
   val shapes=MaterialTheme.shapes;val t=MaterialTheme.typography;val colors=MaterialTheme.colorScheme
   SideEffect{actualRadius=radius(shapes.extraSmall);chipRadius=radius(shapes.small);title=t.titleMedium.fontSize;keptSelection=colors.primaryContainer!=colors.surfaceContainerHigh}
   MaterialTheme(shapes=if(oldControl)Shapes() else shapes) {
    OutlinedTextField(value,{value=it},label={Text("Campo")},singleLine=true,modifier=Modifier.width(280.dp).testTag("field"))
   }
  }}
  ui.onNodeWithTag("field").performClick().performTextInputSelection(TextRange(1,4))
  val before=ui.onNodeWithTag("field").getUnclippedBoundsInRoot()
  ui.runOnIdle{oldControl=false};ui.waitForIdle()
  ui.onNodeWithTag("field").assertIsFocused()
  val after=ui.onNodeWithTag("field").getUnclippedBoundsInRoot()
  assertEquals(before,after);assertEquals(TextRange(1,4),value.selection)
  assertEquals(10f,actualRadius,0f);assertEquals(10f,chipRadius,0f)
  assertSame(FocusDesign.shapes(),FocusDesign.shapes())
  assertEquals(17.sp,title);assertTrue(keptSelection)
  assertTrue(after.bottom-after.top>=48.dp)
 }
 @Test fun darkFieldsKeepBoundsFocusSelectionAndRoles()=fields("dark")
 @Test fun lightFieldsKeepBoundsFocusSelectionAndRoles()=fields("light")
 @Test fun legacyOptOutRestoresTheOriginalShapeDefaultsEvenInsideFocus() {
  var small=0f;var extra=0f
  ui.setContent {SpotlightTheme("dark",AppConfig(focusInterface=true)) {
   SpotlightTheme("dark",AppConfig(focusInterface=false)) {
    val shapes=MaterialTheme.shapes;SideEffect{small=radius(shapes.small);extra=radius(shapes.extraSmall)}
    Text("Legacy")
   }
  }}
  ui.waitForIdle();assertEquals(8f,small,0f);assertEquals(4f,extra,0f)
 }
}
