package dev.bautista.spotlight.ui

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import dev.bautista.spotlight.search.Action
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=android.app.Application::class)
class FocusEditorStateTest {
 private val scope=object:SaverScope {override fun canBeSaved(value:Any)=true}
 @Test fun restorationKeepsIndependentEditorsAndCursorButNeverPreparedLaunch() {
  val spotify=Action.ComposeSearch("spotify","com.spotify.music")
  val vivaldi=Action.ComposeSearch("vivaldi","com.vivaldi.browser")
  val editors=FocusEditors().apply {
   global=TextFieldValue("Vivaldi",TextRange(1,4))
   edit(spotify,TextFieldValue("Luna",TextRange(2)))
   edit(vivaldi,TextFieldValue("android tablet",TextRange(3,7)))
  }
  val saver=FocusEditors.saver(false)
  val saved=with(saver){scope.save(editors)}!!
  val restored=saver.restore(saved)!!
  assertEquals(editors.global,restored.global)
  assertEquals(editors.value(spotify),restored.value(spotify))
  assertEquals(editors.value(vivaldi),restored.value(vivaldi))
  assertFalse(saved.contains("Intent"));assertFalse(saved.contains("prepared"))
  val review=FocusSearchReviewController();review.prepare(vivaldi.copy(query="android tablet"))
  assertNotNull(review.take());assertNull(review.take())
  assertNull(FocusSearchReviewController().prepared)
 }
 @Test fun privateEditorsAreNotSavedAndMalformedRestorationCannotCreateAnAction() {
  assertNull(with(FocusEditors.saver(true)){scope.save(FocusEditors("private text"))})
  val restored=FocusEditors.saver(false).restore("not-json")!!
  assertEquals("",restored.global.text);assertTrue(restored.drafts.isEmpty())
 }
 @Test fun configuredAccentIsContrastAdjustedAcrossAllFocusMaterials() {
  for(dark in listOf(false,true))for(raw in listOf(0xFF0072E8,0xFF566172,0xFF7250CC,0xFFBF3A62,0xFF10B981,0xFFFFB878))for(tone in listOf("neutral","warm","cool")) {
   val base=if(dark)androidx.compose.material3.darkColorScheme() else androidx.compose.material3.lightColorScheme()
   val palette=FocusDesign.palette(base.copy(primary=androidx.compose.ui.graphics.Color(raw)),dark,dev.bautista.spotlight.data.Config(surfaceTone=tone))
   for(surface in listOf(palette.surface,palette.surfaceContainerLow,palette.surfaceContainerHigh)) {
    assertTrue(InspectorAccent.contrast(palette.primary,surface)>=4.5f)
    assertTrue(InspectorAccent.contrast(palette.onSurfaceVariant,surface)>=4.5f)
   }
  }
 }

 @Test fun publicAndroidSearchIsNotPresentedAsAnInternalWebResult() {
  val app=dev.bautista.spotlight.control.ActionCatalog.searchAction("app","fixture.search","Luna") as Action.InApp
  val web=dev.bautista.spotlight.control.ActionCatalog.searchAction("app-web-search","fixture.web","Luna") as Action.InApp
  assertEquals(android.content.Intent.ACTION_SEARCH,app.intent.action)
  assertEquals(android.content.Intent.ACTION_WEB_SEARCH,web.intent.action)
  assertEquals("Buscar en la aplicación",focusSearchActionTitle("app"))
  assertEquals("Buscar en la aplicación",focusSearchPlaceholder("app"))
  assertEquals("Buscar en la web",focusSearchActionTitle("app-web-search"))
 }

}
