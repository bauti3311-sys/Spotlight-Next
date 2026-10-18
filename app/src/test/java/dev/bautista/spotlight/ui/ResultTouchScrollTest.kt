package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Real result rows inside a scrolling Compose list; no PDF provider or external viewer needed. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], qualifiers="w1280dp-h800dp", application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ResultTouchScrollTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private var selectedId by mutableStateOf<String?>(null)
    private var selections=0
    private var opens=0
    private var details=0
    private lateinit var scroll:LazyListState

    private fun show() {
        ui.setContent {
            MaterialTheme {
                scroll=rememberLazyListState()
                LazyColumn(state=scroll,modifier=Modifier.width(720.dp).height(240.dp).testTag("documents")) {
                    items(18) { i ->
                        val item=Result("pdf:$i","Documento $i.pdf","PDF autorizado","Archivos",0,
                            Action.Copy("fixture"),mime="application/pdf")
                        ResultRow(item,selectedId==item.id,false,
                            onOpen={opens++},onDetails={details++},onFavorite={},animations=false,
                            onInteraction={selections++;selectedId=item.id})
                    }
                }
            }
        }
        ui.waitForIdle()
    }

    @Test fun touchDownAndCancelledGestureDoNotSelectOrOpenPdf() {
        show()
        ui.onNodeWithTag("result:pdf:0").performTouchInput { down(center) }
        ui.runOnIdle { assertNull("Touch down is not a PDF selection",selectedId);assertEquals(0,selections);assertEquals(0,opens);assertEquals(0,details) }
        ui.onNodeWithTag("result:pdf:0").performTouchInput { cancel() }
        ui.runOnIdle { assertNull(selectedId);assertEquals(0,selections);assertEquals(0,opens);assertEquals(0,details) }
    }

    @Test fun draggingAcrossPdfRowsScrollsWithoutSelectionPreviewOrLaunch() {
        show()
        ui.onNodeWithTag("documents").performTouchInput {
            swipe(Offset(center.x,190.dp.toPx()),Offset(center.x,30.dp.toPx()),450)
        }
        ui.runOnIdle {
            assertTrue("Gesture must really scroll the document list",scroll.firstVisibleItemIndex>0 || scroll.firstVisibleItemScrollOffset>0)
            assertNull("Scrolling must not change selection (or trigger automatic Quick Look)",selectedId)
            assertEquals(0,selections);assertEquals(0,opens);assertEquals(0,details)
        }
    }

    @Test fun completedTapSelectsAndInvokesThePrimaryCallbackExactlyOnce() {
        show()
        ui.onNodeWithTag("result:pdf:0").performTouchInput { click() }
        ui.runOnIdle { assertEquals("pdf:0",selectedId);assertEquals(1,selections);assertEquals(1,opens);assertEquals(0,details) }
    }

    @Test fun longPressShowsActionsWithoutInvokingPrimaryCallback() {
        show()
        ui.onNodeWithTag("result:pdf:0").performTouchInput { longClick() }
        ui.runOnIdle { assertEquals("pdf:0",selectedId);assertEquals(1,selections);assertEquals(0,opens);assertEquals(1,details) }
    }

    @Test fun accessibilityClickKeepsTheSameSelectionAndPrimaryContract() {
        show()
        ui.onNodeWithTag("result:pdf:0").performClick()
        ui.runOnIdle { assertEquals("pdf:0",selectedId);assertEquals(1,selections);assertEquals(1,opens);assertEquals(0,details) }
    }
}
