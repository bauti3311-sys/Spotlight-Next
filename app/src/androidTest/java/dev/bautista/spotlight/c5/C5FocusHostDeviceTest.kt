package dev.bautista.spotlight.c5

import android.app.Dialog
import android.graphics.Rect
import android.os.SystemClock
import android.view.*
import android.widget.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bautista.spotlight.MainActivity
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.index.*
import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.ui.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Isolated test dialog on the target Activity. NOT C5 enabled in the productive Focus bar. */
@RunWith(AndroidJUnit4::class)
class C5FocusHostDeviceTest {
    @Test fun touchScrollNeverOpensAndHostStopClearsTheIsolatedStudioPreview()=runBlocking {
        val lab=C5PhysicalLab()
        lab.runCase("focus-host") {
            val c=MutableStateFlow(Config(files=true,contentIndex=true,online=false,focusInterface=true))
            val index=lab.index{c.value};val file=lab.file("host.pdf",lab.asset("real-three-pages.pdf"))
            lab.seed(index,file,"application/pdf",c.value)
            val parent=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
            val state=DocumentCitationState(index,c,parent)
            try {
                state.query("presupuesto café").join();val ref=CitationRef(state.visible.value.single().sourceId)
                val opens=AtomicInteger();val copies=AtomicInteger();val copyText=AtomicReference("")
                lateinit var dialog:Dialog;lateinit var scroll:ScrollView;lateinit var open:Button;lateinit var copy:Button
                var opened by mutableStateOf<Result?>(null)
                ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                    scenario.onActivity { activity ->
                        val list=LinearLayout(activity).apply{orientation=LinearLayout.VERTICAL}
                        val quote=TextView(activity)
                        list.addView(quote)
                        list.addView(TextView(activity).apply{text="Fixture C5 — desplazamiento sin apertura\n".repeat(45)})
                        copy=Button(activity).apply{text="Copiar cita (adaptador en memoria)";setOnClickListener{parent.launch {
                            state.withValidated(ref){citation->
                                copies.incrementAndGet();copyText.set(citation.quote+"\n"+citation.documentTitle+" · página "+(citation.page?.toString() ?: "no comprobada")+" · SHA-256 "+citation.sourceSha256+"\n"+citation.documentUri)
                            }
                        }}}
                        open=Button(activity).apply{text="Abrir fuente explícitamente";setOnClickListener{parent.launch {
                            state.withValidated(ref){citation->
                                opens.incrementAndGet();opened=Result("c5-fixture",citation.documentTitle,"Fixture","Archivos",1,
                                    Action.Launch(android.content.Intent(android.content.Intent.ACTION_VIEW).setDataAndType(android.net.Uri.parse(citation.documentUri),"application/pdf")))
                            }
                        }}}
                        list.addView(copy);list.addView(open)
                        val compose=ComposeView(activity).apply {
                            setViewTreeLifecycleOwner(activity);setViewTreeSavedStateRegistryOwner(activity)
                            setContent {SpotlightTheme("dark",c.value){StudioSurface("inspector") {
                                opened?.let{item->AuthorizedFilePreview(activity,item,false,0,{})}
                            }}}
                        }
                        list.addView(compose,LinearLayout.LayoutParams(-1,360))
                        scroll=ScrollView(activity).apply{addView(list)}
                        dialog=Dialog(activity).apply{setContentView(scroll);setOnDismissListener{state.invalidate();opened=null}}
                        dialog.show();dialog.window!!.setLayout(-1,-1)
                        parent.launch{state.visible.collect{items->quote.text=items.firstOrNull()?.quote.orEmpty();if(items.isEmpty())opened=null}}
                        activity.lifecycle.addObserver(androidx.lifecycle.LifecycleEventObserver{_,event->
                            if(event in setOf(Lifecycle.Event.ON_PAUSE,Lifecycle.Event.ON_STOP,Lifecycle.Event.ON_DESTROY)){
                                state.invalidate();opened=null
                            }
                            if(event==Lifecycle.Event.ON_DESTROY)dialog.dismiss()
                        })
                        dialog.window!!.decorView.viewTreeObserver.addOnWindowFocusChangeListener{focused->if(!focused){state.invalidate();opened=null}}
                    }
                    lab.instrumentation.waitForIdleSync()
                    withTimeout(5000){var laidOut=false;while(!laidOut){lab.instrumentation.runOnMainSync{laidOut=scroll.isLaidOut && scroll.width>0};if(!laidOut)delay(16)}}
                    fun touch(view:View,swipe:Boolean) {
                        val rect=Rect();lab.instrumentation.runOnMainSync{assertTrue(view.getGlobalVisibleRect(rect))}
                        val x=rect.centerX().toFloat();val y=if(swipe)rect.top+rect.height()*.7f else rect.centerY().toFloat()
                        val start=SystemClock.uptimeMillis()
                        fun send(action:Int,time:Long,at:Float){MotionEvent.obtain(start,time,action,x,at,0).also{e->try{lab.instrumentation.sendPointerSync(e)}finally{e.recycle()}}}
                        send(MotionEvent.ACTION_DOWN,start,y)
                        if(swipe)for(i in 1..8){SystemClock.sleep(16);send(MotionEvent.ACTION_MOVE,SystemClock.uptimeMillis(),y-i*rect.height()*.05f)}
                        send(MotionEvent.ACTION_UP,SystemClock.uptimeMillis(),if(swipe)y-rect.height()*.4f else y)
                        lab.instrumentation.waitForIdleSync()
                    }
                    touch(scroll,true);assertEquals(0,opens.get());assertEquals(0,copies.get())
                    lab.instrumentation.runOnMainSync{copy.requestRectangleOnScreen(Rect(0,0,copy.width,copy.height),true)}
                    lab.instrumentation.waitForIdleSync();touch(copy,false)
                    withTimeout(5000){while(copies.get()==0)delay(10)};assertTrue(copyText.get().contains("SHA-256"))
                    lab.instrumentation.runOnMainSync{open.requestRectangleOnScreen(Rect(0,0,open.width,open.height),true)}
                    lab.instrumentation.waitForIdleSync()
                    touch(open,false);withTimeout(5000){while(opens.get()==0)delay(10)};assertEquals(1,opens.get())
                    // Activity stop invalidates synchronously; resume never restores an old plaintext snapshot.
                    scenario.moveToState(Lifecycle.State.CREATED)
                    assertTrue(state.visible.value.isEmpty());assertNull(opened)
                    scenario.moveToState(Lifecycle.State.RESUMED)
                    assertTrue(state.visible.value.isEmpty())
                    lab.instrumentation.runOnMainSync{dialog.dismiss()};copyText.set("")
                }
                JSONObject().put("scroll_open_requests",0).put("explicit_open_requests",opens.get())
                    .put("copy_adapter_calls",copies.get()).put("clipboard_was_not_modified",true)
                    .put("open_counter_is_request_not_render_completion",true)
                    .put("test_dialog_not_productive_bar",true)
            } finally {state.close();parent.cancel()}
        }
    }
}
