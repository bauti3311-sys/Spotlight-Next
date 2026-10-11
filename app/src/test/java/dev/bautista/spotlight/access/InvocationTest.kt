package dev.bautista.spotlight.access

import android.content.Intent
import android.view.KeyEvent
import dev.bautista.spotlight.InvokeActivity
import dev.bautista.spotlight.MainActivity
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.overlay.OverlayService
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class InvocationTest {
    @Before fun allow(){ShadowSettings.setCanDrawOverlays(true)}
    @Test fun physicalShortcutStartsOnlyOverlayAndConsumesRepeat(){
        val controller=Robolectric.buildService(ShortcutService::class.java).create();val service=controller.get()
        try{
            val meta=KeyEvent.META_CTRL_ON or KeyEvent.META_ALT_ON
            assertTrue(service.onKeyEvent(KeyEvent(0,1,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_SPACE,0,meta)))
            val shadow=Shadows.shadowOf(service);val intent=shadow.nextStartedService
            assertEquals(OverlayService::class.java.name,intent.component!!.className);assertEquals("toggle",intent.action)
            assertNull(shadow.nextStartedActivity)
            assertTrue(service.onKeyEvent(KeyEvent(0,2,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_SPACE,1,meta)))
            assertNull(shadow.nextStartedService)
            assertTrue(service.onKeyEvent(KeyEvent(0,3,KeyEvent.ACTION_UP,KeyEvent.KEYCODE_SPACE,0,0)))
            assertFalse(service.onKeyEvent(KeyEvent(0,4,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_SPACE,0,0)))
        }finally{controller.destroy()}
    }
    @Test fun missingOverlayPermissionDoesNotOpenSettingsTask(){
        ShadowSettings.setCanDrawOverlays(false)
        val controller=Robolectric.buildService(ShortcutService::class.java).create();val service=controller.get()
        try{assertFalse(service.onKeyEvent(KeyEvent(0,1,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_SPACE,0,KeyEvent.META_CTRL_ON or KeyEvent.META_ALT_ON)));assertNull(Shadows.shadowOf(service).nextStartedActivity);assertNull(Shadows.shadowOf(service).nextStartedService)}finally{controller.destroy()}
    }
    @Test fun trampolineFinishesWithoutLaunchingMainActivity(){
        val controller=Robolectric.buildActivity(InvokeActivity::class.java,Intent().putExtra("query","hola")).create();val activity=controller.get()
        assertTrue(activity.isFinishing);assertNull(Shadows.shadowOf(activity).nextStartedActivity)
        assertEquals(OverlayService::class.java.name,Shadows.shadowOf(activity).nextStartedService.component!!.className)
        controller.destroy()
    }
}
