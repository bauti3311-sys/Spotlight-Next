package dev.bautista.spotlight.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import dev.bautista.spotlight.SpotlightApp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class SharedImportPanelTest {
    @get:Rule val ui=createComposeRule()
    @Test fun importShowsRealDocumentAndByteCountersAndExplicitCancel() {
        var cancelled=false
        ui.setContent{SpotlightTheme{SharedImportPanel(SharedImportProgress(2,3,131072)){cancelled=true}}}
        ui.onNodeWithText("Documento 2 de 3").assertIsDisplayed()
        ui.onNodeWithText("128 KB copiados localmente").assertIsDisplayed()
        ui.onNodeWithTag("shared-import-cancel").performClick();assertTrue(cancelled)
    }
}
