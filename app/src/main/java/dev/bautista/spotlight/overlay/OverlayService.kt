package dev.bautista.spotlight.overlay

import android.app.*
import android.content.*
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.*
import androidx.savedstate.*
import dev.bautista.spotlight.*
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.ui.SpotlightPanel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class OverlayService : LifecycleService(), SavedStateRegistryOwner, ViewModelStoreOwner {
    private val saved = SavedStateRegistryController.create(this)
    override val savedStateRegistry
        get() = saved.savedStateRegistry

    override val viewModelStore = ViewModelStore()
    private var view: WindowFocusHost? = null
    private var composition: ComposeView? = null
    private var closing=false
    private var focusSeen = false
    private var showJob: Job? = null
    private val wm by lazy { getSystemService(WindowManager::class.java) }
    private val app
        get() = application as SpotlightApp

    private var config by androidx.compose.runtime.mutableStateOf(Config())
    private var initialQuery = ""
    private var invocation = "touch"
    private var availableHeightDp by androidx.compose.runtime.mutableFloatStateOf(480f)

    private fun updateAvailableHeight(imeBottom: Int = 0) {
        val dm = resources.displayMetrics
        val top = AdaptiveLayout.bounds(dm.widthPixels,dm.heightPixels,dm.density,imeBottom,config).top
        availableHeightDp =
            ((dm.heightPixels - top - imeBottom) / dm.density - 24f).coerceAtLeast(100f)
    }

    private val clipboard by lazy { getSystemService(ClipboardManager::class.java) }
    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener { captureClipboard() }

    private fun captureClipboard() {
        if (config.privateMode || app.preferences.isPrivate || (!config.clipboard && !config.captureContext) || view?.hasWindowFocus() != true) return
        runCatching {
            val clip = clipboard.primaryClip ?: return
            if (
                clip.description.extras?.getBoolean("android.content.extra.IS_SENSITIVE", false) ==
                    true
            )
                return
            val text = clip.getItemAt(0).text?.toString() ?: return
            if(config.clipboard)app.local.capture(text)
            if(config.captureContext)app.contexts.offerAutomatic(text)
        }
    }

    override fun onCreate() {
        saved.performAttach()
        saved.performRestore(null)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == "close") {
            close()
            return START_NOT_STICKY
        }
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        val channel =
            NotificationChannel("overlay", "Spotlight visible", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val pending =
            PendingIntent.getService(
                this,
                1,
                Intent(this, OverlayService::class.java).setAction("close"),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val settingsPending =
            PendingIntent.getActivity(
                this,
                2,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        startForeground(
            1,
            NotificationCompat.Builder(this, "overlay")
                .setSmallIcon(dev.bautista.spotlight.R.drawable.ic_spotlight)
                .setContentTitle("Spotlight está abierto")
                .setContentText("Usá Cerrar para salir de la ventana")
                .setContentIntent(settingsPending)
                .addAction(dev.bautista.spotlight.R.drawable.ic_spotlight, "Cerrar", pending)
                .setOngoing(true)
                .build(),
        )
        if (view != null || showJob?.isActive == true) {
            close()
            return START_NOT_STICKY
        }
        initialQuery = intent?.getStringExtra("query")?.take(4000) ?: ""
        invocation=intent?.getStringExtra("invocation") ?: "touch"
        app.diagnostics.beginOpening()
        showJob =
            lifecycleScope.launch {
                app.preferences.config.collect { next ->
                    val old=config
                    config=next
                    if(view==null && !closing)show()
                    else if(old.width!=next.width || old.position!=next.position || old.landscapeWidth!=next.landscapeWidth || old.landscapePosition!=next.landscapePosition) {
                        updateAvailableHeight();view?.let{runCatching{wm.updateViewLayout(it,params())}}
                    }
                }
            }
        return START_NOT_STICKY
    }

    private fun params(): WindowManager.LayoutParams {
        val dm = resources.displayMetrics
        val bounds=AdaptiveLayout.bounds(dm.widthPixels,dm.heightPixels,dm.density,0,config)
        val width=bounds.width
        return WindowManager.LayoutParams(
                width,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT,
            )
            .apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = bounds.top
                softInputMode =
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
                title = "Spotlight"
                // Keep all pixels outside the actual capsule/circles transparent.
                // FLAG_BLUR_BEHIND blurs the rectangular window footprint on One UI.
                dimAmount = 0f
            }
    }

    private fun show() {
        updateAvailableHeight()
        val panel =
            ComposeView(this).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setViewTreeLifecycleOwner(this@OverlayService)
                setViewTreeSavedStateRegistryOwner(this@OverlayService)
                setViewTreeViewModelStoreOwner(this@OverlayService)
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent {
                    SpotlightPanel(
                        app,
                        config,
                        onClose = { close() },
                        initialQuery = initialQuery,
                        availableHeightDp = availableHeightDp,
                        invocation = invocation,
                    )
                }
            }
        focusSeen = false
        val host = createHost(panel)
        ViewCompat.setOnApplyWindowInsetsListener(host) { _, insets ->
            updateAvailableHeight(
                maxOf(
                    insets.getInsets(WindowInsetsCompat.Type.ime()).bottom,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom,
                )
            )
            insets
        }
        try {
            wm.addView(host, params())
            view = host
            composition = panel
            host.viewTreeObserver.addOnDrawListener(object:android.view.ViewTreeObserver.OnDrawListener {
                var reported=false
                override fun onDraw(){if(!reported){reported=true;app.diagnostics.markVisible()}}
            })
            // Visible on the first frame: no competing window/Compose entrance animations.
            clipboard.addPrimaryClipChangedListener(clipListener)
            host.postDelayed(
                {
                    if (view === host && !focusSeen) {
                        Toast.makeText(
                                this,
                                "Esta pantalla no permitió enfocar Spotlight. Abrilo desde otra app.",
                                Toast.LENGTH_LONG,
                            )
                            .show()
                        close()
                    }
                },
                2500L,
            )
        } catch (e: RuntimeException) {
            Toast.makeText(
                    this,
                    "No se pudo mostrar la ventana. Revisá el permiso de superposición.",
                    Toast.LENGTH_LONG,
                )
                .show()
            panel.disposeComposition()
            stopSelf()
        }
    }

    // WindowRecomposer is installed on the window root, so owners must be on the
    // host itself, not only on the ComposeView child.
    internal fun createHost(panel: ComposeView): WindowFocusHost {
        return WindowFocusHost(
                this,
                onOutside = { close() },
                onFocus = { gained ->
                    if (gained) {
                        focusSeen = true
                        panel.post { captureClipboard() }
                    }
                    // IME, selection toolbars and child windows can take focus.
                    // Outside touch and explicit dismiss remain the close signals.
                },
            )
            .apply {
                setViewTreeLifecycleOwner(this@OverlayService)
                setViewTreeSavedStateRegistryOwner(this@OverlayService)
                setViewTreeViewModelStoreOwner(this@OverlayService)
                addView(
                    panel,
                    android.widget.FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    ),
                )
            }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateAvailableHeight()
        view?.let { runCatching { wm.updateViewLayout(it, params()) } }
    }

    private fun motionAllowed()=config.animations && runCatching { Settings.Global.getFloat(contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,1f)>0 }.getOrDefault(false)
    private fun close() {
        if(closing) return
        closing=true
        clipboard.removePrimaryClipChangedListener(clipListener)
        showJob?.cancel()
        val host=view
        if(host!=null && host.isAttachedToWindow && motionAllowed()) {
            host.animate().cancel()
            host.animate().alpha(0f).scaleX(.99f).scaleY(.99f).translationY(-3f*resources.displayMetrics.density).setDuration(90).withEndAction {if(view===host)finishClose()}.start()
        } else finishClose()
    }
    private fun finishClose() {
        clipboard.removePrimaryClipChangedListener(clipListener)
        showJob?.cancel()
        composition?.disposeComposition()
        composition = null
        view?.let { runCatching { wm.removeViewImmediate(it) } }
        view = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        closing=true
        view?.animate()?.cancel()
        clipboard.removePrimaryClipChangedListener(clipListener)
        composition?.disposeComposition()
        composition = null
        view?.let { runCatching { wm.removeViewImmediate(it) } }
        view = null
        viewModelStore.clear()
        super.onDestroy()
    }
}
