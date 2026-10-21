package dev.bautista.spotlight.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.*
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.compose.runtime.*
import androidx.compose.ui.platform.AbstractComposeView

/** Bounded child of the existing window, never another TYPE_APPLICATION_OVERLAY.
 * Public input flags let touches outside this rectangle reach the underlying window.
 * The parent retains its measured size and its result list/scroll position.
 */
internal class ActionPopupHost(
    context:Context,
    private val anchor:View,
    private val dismiss:()->Unit,
    private val back:()->Unit,
) : AbstractComposeView(context) {
    private val manager=context.getSystemService(WindowManager::class.java)
    private var attached=false
    private var bounds=Rect()
    private var anchorBounds=Rect()
    private var suppliedHeight=Int.MAX_VALUE
    var maximumHeight by mutableIntStateOf(1)
        private set
    var body:(@Composable ()->Unit)? by mutableStateOf(null)
    private val params=WindowManager.LayoutParams().apply {
        type=WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL
        token=anchor.applicationWindowToken
        flags=WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        format=PixelFormat.TRANSLUCENT
        gravity=Gravity.TOP or Gravity.START
        height=WindowManager.LayoutParams.WRAP_CONTENT
        softInputMode=WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
        title="Spotlight · Acciones"
        if(Build.VERSION.SDK_INT>=30)setFitInsetsTypes(0) // Safe frame is applied explicitly, once.
    }
    init { isFocusable=true;isFocusableInTouchMode=true }
    private val layoutListener=ViewTreeObserver.OnGlobalLayoutListener { reposition() }
    private var unregisterBack:(()->Unit)?=null

    @Composable override fun Content() { body?.invoke() }

    fun show(rect:Rect,widthPx:Int,heightLimitPx:Int) {
        anchorBounds=Rect(rect);suppliedHeight=heightLimitPx;params.width=widthPx
        updateSafeBounds()
        val desired=ActionPopupPosition.place(anchorBounds,bounds,params.width,maximumHeight,(56*resources.displayMetrics.density).toInt(),horizontalOutset())
        params.x=desired.left;params.y=desired.top
        manager.addView(this,params);attached=true
        anchor.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        requestFocus()
    }

    fun update(rect:Rect,widthPx:Int,heightLimitPx:Int) {
        val changed=params.width!=widthPx
        anchorBounds=Rect(rect);suppliedHeight=heightLimitPx;params.width=widthPx
        reposition()
        if(changed && attached)manager.updateViewLayout(this,params)
    }

    private fun horizontalOutset()=if(anchorBounds.width()>=600*resources.displayMetrics.density)(96*resources.displayMetrics.density).toInt() else 0

    private fun updateSafeBounds() {
        anchor.getWindowVisibleDisplayFrame(bounds)
        if(bounds.width()<=0 || bounds.height()<=0)bounds.set(0,0,resources.displayMetrics.widthPixels,resources.displayMetrics.heightPixels)
        params.width=params.width.coerceIn(1,bounds.width().coerceAtLeast(1))
        maximumHeight=minOf(suppliedHeight,(bounds.height()-16*resources.displayMetrics.density).toInt()).coerceAtLeast(1)
    }

    private fun reposition() {
        updateSafeBounds()
        if(!attached)return
        val placed=ActionPopupPosition.place(anchorBounds,bounds,params.width,height.takeIf{it>0} ?: maximumHeight,(56*resources.displayMetrics.density).toInt(),horizontalOutset())
        if(params.x!=placed.left || params.y!=placed.top || layoutParams?.width!=params.width) {
            params.x=placed.left;params.y=placed.top
            manager.updateViewLayout(this,params)
        }
    }

    override fun onSizeChanged(w:Int,h:Int,oldw:Int,oldh:Int) {
        super.onSizeChanged(w,h,oldw,oldh)
        post { reposition() }
    }

    override fun onApplyWindowInsets(insets:WindowInsets):WindowInsets {
        post { reposition() }
        return super.onApplyWindowInsets(insets)
    }

    override fun dispatchKeyEvent(event:KeyEvent):Boolean {
        if(event.keyCode==KeyEvent.KEYCODE_BACK) {
            if(event.action==KeyEvent.ACTION_UP && !event.isCanceled)back()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onTouchEvent(event:MotionEvent):Boolean {
        if(event.action==MotionEvent.ACTION_OUTSIDE || event.action==MotionEvent.ACTION_DOWN &&
            (event.x<0 || event.y<0 || event.x>=width || event.y>=height)) {
            dismiss();return true
        }
        return super.onTouchEvent(event)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if(Build.VERSION.SDK_INT>=33) {
            unregisterBack=Api33.register(this,back)
        }
    }

    fun close() {
        unregisterBack?.invoke();unregisterBack=null
        if(anchor.viewTreeObserver.isAlive)anchor.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        if(attached){attached=false;manager.removeViewImmediate(this)}
        disposeComposition()
    }

    private object Api33 {
        @androidx.annotation.RequiresApi(33)
        fun register(view:View,back:()->Unit):()->Unit {
            val dispatcher=view.findOnBackInvokedDispatcher()
            val callback=OnBackInvokedCallback{back()}
            dispatcher?.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT,callback)
            return {dispatcher?.unregisterOnBackInvokedCallback(callback);Unit}
        }
    }
}

/** Pixel geometry, shared by the attached host and tests with real viewport/IME budgets. */
internal object ActionPopupPosition {
    fun place(anchor:Rect,safe:Rect,width:Int,height:Int,searchInset:Int=56,horizontalOutset:Int=0):Rect {
        val w=width.coerceIn(1,safe.width().coerceAtLeast(1))
        val h=height.coerceIn(1,safe.height().coerceAtLeast(1))
        val x=(anchor.right-w+horizontalOutset).coerceIn(safe.left,(safe.right-w).coerceAtLeast(safe.left))
        val y=(anchor.top+minOf(searchInset,anchor.height())).coerceIn(safe.top,(safe.bottom-h).coerceAtLeast(safe.top))
        return Rect(x,y,x+w,y+h)
    }
}
