package dev.bautista.spotlight.input

import android.app.Application
import android.view.KeyEvent
import dev.bautista.spotlight.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class KeyboardBindingsTest {
    @Test fun defaultsAndLegacyClipboardPreferenceRemainCompatible() {
        assertEquals(KeyChord(KeyEvent.KEYCODE_K),KeyboardBindings.resolve(dev.bautista.spotlight.data.Config(),"actions"))
        assertEquals(KeyChord(KeyEvent.KEYCODE_P),KeyboardBindings.resolve(dev.bautista.spotlight.data.Config(clipboardShortcut="ctrl_p"),"clipboard"))
        assertNull(KeyboardBindings.resolve(dev.bautista.spotlight.data.Config(clipboardShortcut="off"),"clipboard"))
    }
    @Test fun conflictsIncludeOtherCommandsGlobalAndTextEditing() {
        val c=dev.bautista.spotlight.data.Config()
        assertNotNull(KeyboardBindings.conflict(c,"expand",KeyChord(KeyEvent.KEYCODE_K)))
        assertNotNull(KeyboardBindings.conflict(c,"actions",KeyChord(KeyEvent.KEYCODE_C)))
        assertNotNull(KeyboardBindings.conflict(c,"actions",KeyChord(KeyEvent.KEYCODE_V)))
        assertNotNull(KeyboardBindings.conflict(c.copy(shortcut="ctrl"),"actions",KeyChord(KeyEvent.KEYCODE_SPACE)))
        assertNotNull(KeyboardBindings.conflict(c,"actions",KeyChord(KeyEvent.KEYCODE_TAB,ctrl=false,alt=true)))
        assertNotNull(KeyboardBindings.conflict(c,"actions",KeyChord(KeyEvent.KEYCODE_L,ctrl=false)))
    }
    @Test fun newBindingReplacesOnlyItsOwnEntryAndModifiersMatchExactly() {
        val c=KeyboardBindings.assign(dev.bautista.spotlight.data.Config(),"actions",KeyChord(KeyEvent.KEYCODE_L))
        assertEquals(KeyChord(KeyEvent.KEYCODE_M),KeyboardBindings.resolve(c,"expand"))
        fun event(meta:Int)=KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_L,0,meta)
        assertTrue(KeyboardBindings.matches(c,"actions",event(KeyEvent.META_CTRL_ON)))
        assertFalse(KeyboardBindings.matches(c,"actions",event(KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON)))
        assertFalse(KeyboardBindings.matches(c,"actions",event(0)))
        assertEquals(c.keyBindings,ConfigTransfer.decode(ConfigTransfer.encode(c),dev.bautista.spotlight.data.Config()).keyBindings)
    }
    @Test fun malformedOrDuplicateEntriesAreRejected() {
        assertFalse(KeyboardBindings.validEntries(setOf("actions=anything")))
        assertFalse(KeyboardBindings.validEntries(setOf("unknown=1:0:0:45")))
        assertFalse(KeyboardBindings.validEntries(setOf("actions=1:0:0:45","actions=1:0:0:46")))
    }
}
