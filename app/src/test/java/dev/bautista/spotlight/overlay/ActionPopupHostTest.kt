package dev.bautista.spotlight.overlay

import androidx.activity.ComponentActivity
import android.graphics.Rect
import android.view.*
import android.widget.FrameLayout
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class ActionPopupHostTest {
    @Test fun tabletMenuCanExceedCompactParentWithoutMovingIt() {
        val anchor=Rect(150,120,890,294)
        val original=Rect(anchor)
        val placed=ActionPopupPosition.place(anchor,Rect(0,24,1280,800),390,480)
        assertEquals(original,anchor)
        assertTrue(placed.height()>anchor.height())
        assertTrue(placed.top<anchor.bottom)
        assertTrue(Rect(0,24,1280,800).contains(placed))
    }
    @Test fun phoneImeAndRotationStayInsideUsableBounds() {
        for(safe in listOf(Rect(0,24,390,844),Rect(0,24,390,380),Rect(0,24,844,330))) {
            val placed=ActionPopupPosition.place(Rect(16,110,safe.right-16,284),safe,390,480)
            assertTrue(safe.toString(),safe.contains(placed))
        }
    }
    @Test fun childTokenAndTouchRegionRemainBoundedAndOutsideIsNotModal() {
        val activity=Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val anchor=FrameLayout(activity)
        activity.setContentView(anchor)
        var outside=0;var backs=0
        val popup=ActionPopupHost(activity,anchor,{outside++},{backs++})
        popup.setViewTreeLifecycleOwner(activity)
        popup.setViewTreeViewModelStoreOwner(activity)
        popup.setViewTreeSavedStateRegistryOwner(activity)
        popup.show(Rect(16,40,350,214),320,480)
        val params=popup.layoutParams as WindowManager.LayoutParams
        assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL,params.type)
        assertEquals(anchor.applicationWindowToken,params.token)
        assertEquals(320,params.width)
        assertEquals(WindowManager.LayoutParams.WRAP_CONTENT,params.height)
        assertTrue(params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL!=0)
        assertTrue(params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE==0)
        popup.onTouchEvent(MotionEvent.obtain(0,0,MotionEvent.ACTION_OUTSIDE,0f,0f,0))
        assertEquals(1,outside)
        popup.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_BACK))
        assertEquals(1,backs)
        popup.close();popup.close()
        assertFalse(popup.isAttachedToWindow)
        activity.finish()
    }
}
