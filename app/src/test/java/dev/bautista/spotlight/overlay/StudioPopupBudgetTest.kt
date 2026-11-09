package dev.bautista.spotlight.overlay

import android.graphics.Rect
import android.view.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1100dp-h900dp-mdpi")
class StudioPopupBudgetTest {
 private fun checkHost(studio:Boolean=true,body:(ActionPopupHost,Rect)->Unit) {
  val a=Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
  val safe=Rect(0,24,1000,696)
  val anchor=object:View(a){override fun getWindowVisibleDisplayFrame(r:Rect){r.set(safe)}}
  a.setContentView(anchor)
  val popup=ActionPopupHost(a,anchor,{},{},studio=studio)
  popup.setViewTreeLifecycleOwner(a);popup.setViewTreeViewModelStoreOwner(a);popup.setViewTreeSavedStateRegistryOwner(a)
  try{popup.show(Rect(90,64,910,340),360,576);body(popup,safe)}finally{popup.close();a.finish()}
 }
 @Test fun focusHeightTracksTheCompactParentInsteadOfTheFullScreen()=checkHost {popup,_->
  assertTrue("Compact parent should not produce a 576px menu",popup.maximumHeight<=320)
 }
 @Test fun focusRightEdgeStaysAttachedToItsParent()=checkHost {popup,_->
  val p=popup.layoutParams as WindowManager.LayoutParams
  assertTrue("Menu must stay within parent's right edge",p.x+p.width<=910)
 }
 @Test fun focusMarginsAndRotationImeUpdatesStayInsideTheFrame()=checkHost {popup,safe->
  for(frame in listOf(Rect(0,24,1000,696),Rect(0,24,360,740),Rect(0,24,780,320),Rect(0,24,360,340))) {
   safe.set(frame)
   popup.update(Rect(16,64,frame.right-16,260),minOf(360,frame.width()-32),576)
   val p=popup.layoutParams as WindowManager.LayoutParams
   val menu=Rect(p.x,p.y,p.x+p.width,p.y+popup.maximumHeight)
   val padded=Rect(frame).apply{inset(8,8)}
   assertTrue("No edge/IME collision: $menu / $padded",padded.contains(menu))
  }
 }
 @Test fun legacyKeepsItsOriginalHeightAndOutset()=checkHost(studio=false){popup,_->
  assertEquals(576,popup.maximumHeight)
  val p=popup.layoutParams as WindowManager.LayoutParams;assertEquals(1000,p.x+p.width)
 }
}
