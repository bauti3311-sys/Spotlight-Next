package dev.bautista.spotlight.overlay

import android.app.*
import android.content.*
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
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
    private val overlayContext: Context by lazy {
        // Android 11 may return a WindowContext after denying its token registration,
        // without throwing. Keep the service manager there; WindowMetrics still applies.
        // From Android 12, manager and views share the registered visual context.
        if (Build.VERSION.SDK_INT >= 31) runCatching {
            val display = getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)
            createDisplayContext(requireNotNull(display))
                .createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null)
        }.getOrNull() ?: this else this
    }
    private val wm by lazy { overlayContext.getSystemService(WindowManager::class.java) }

    private val app
        get() = application as SpotlightApp

    private var config by androidx.compose.runtime.mutableStateOf(Config())
    private var initialQuery = ""
    private var invocation = "touch"
    private var contentExpanded=false
    private val widthTransition = OverlayWidthTransition()
    private var availableHeightDp by androidx.compose.runtime.mutableFloatStateOf(480f)

    // Insets are event-driven; preference and rotation refreshes must not imply IME dismissal.
    private var lastBottomInset = 0
    private var lastSystemBottomInset = 0
    private var lastLoggedGeometry: OverlayBounds? = null

    private fun bounds(): OverlayBounds {
        val screen = OverlayViewport.read(wm, resources, lastSystemBottomInset, view, lastBottomInset)
        return AdaptiveLayout.bounds(screen.width, screen.height, screen.density,
            screen.keyboardOcclusion(lastBottomInset), config, contentExpanded, screen.landscape)
    }

    private fun refreshWindowGeometry(animateWidth: Boolean = false) {
        val geometry = updateAvailableHeight()
        if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0 && lastLoggedGeometry != geometry) {
            lastLoggedGeometry = geometry
            android.util.Log.d("SpotlightOverlay", "Geometry=$geometry bottom=$lastBottomInset system=$lastSystemBottomInset")
        }
        view?.let { host ->
            val next = params(geometry)
            val current = host.layoutParams as? WindowManager.LayoutParams
            // Only content transitions animate. Insets, rotation and manual preferences
            // cancel them immediately so stale frames cannot cross a new safe area.
            widthTransition.move(current?.width ?: next.width, next.width,
                animateWidth && motionAllowed(),
                when (config.movement) { "precise" -> 140L; "liquid_immersive" -> 260L; else -> 190L }) { width ->
                if (view === host && !closing) {
                    val frame = params(geometry).apply { this.width = width }
                    val actual = host.layoutParams as? WindowManager.LayoutParams
                    if (actual?.width != frame.width || actual?.y != frame.y) {
                        runCatching { wm.updateViewLayout(host, frame) }
                    }
                }
            }
        }
    }

    internal fun updateAvailableHeight(imeBottom: Int = lastBottomInset): OverlayBounds {
        lastBottomInset = imeBottom
        return bounds().also { availableHeightDp = it.heightDp }
    }

    private val clipboard by lazy { getSystemService(ClipboardManager::class.java) }
    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener { captureClipboard() }

    private fun captureClipboard() {
        if (!app.preferences.ready || config.privateMode || app.preferences.isPrivate || (!config.clipboard && !config.captureContext) || view?.hasWindowFocus() != true) return
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
                        refreshWindowGeometry()
                    }
                }
            }
        return START_NOT_STICKY
    }

    private fun params(geometry: OverlayBounds = bounds()): WindowManager.LayoutParams {
        val width=geometry.width
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
                y = geometry.top
                softInputMode =
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
                if (Build.VERSION.SDK_INT >= 30) {
                    setFitInsetsTypes(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                    setFitInsetsIgnoringVisibility(true)
                }
                if (Build.VERSION.SDK_INT >= 28) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
                }
                title = "Spotlight"
                // Keep all pixels outside the actual capsule/circles transparent.
                // FLAG_BLUR_BEHIND blurs the rectangular window footprint on One UI.
                dimAmount = 0f
            }
    }

    private fun show() {
        val openingBounds = updateAvailableHeight()
        if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) android.util.Log.d("SpotlightOverlay", "Opening geometry=$openingBounds")
        val panel =
            ComposeView(overlayContext).apply {
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
                        onContentExpansion={expanded->if(contentExpanded!=expanded){contentExpanded=expanded;refreshWindowGeometry(animateWidth = true)}},
                    )
                }
            }
        focusSeen = false
        val host = createHost(panel)
        ViewCompat.setOnApplyWindowInsetsListener(host) { _, insets ->
            lastSystemBottomInset = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()).bottom
            lastBottomInset = maxOf(
                insets.getInsets(WindowInsetsCompat.Type.ime()).bottom,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom,
            )
            refreshWindowGeometry()
            insets
        }
        try {
            wm.addView(host, params(openingBounds))
            view = host
            composition = panel
            ViewCompat.requestApplyInsets(host)
            host.viewTreeObserver.addOnDrawListener(object:android.view.ViewTreeObserver.OnDrawListener {
                var reported=false
                override fun onDraw(){if(!reported){
                    reported=true
                    app.diagnostics.markVisible()
                    if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                        android.util.Log.d("SpotlightOverlay", "First draw: windowFocus=${host.hasWindowFocus()} editorFocus=${panel.hasFocus()}")
                    }
                }}
            })
            // Visible on the first frame: no competing window/Compose entrance animations.
            clipboard.addPrimaryClipChangedListener(clipListener)
            host.postDelayed(
                {
                    if (view === host && !focusSeen) {
                        android.util.Log.w("SpotlightOverlay", "Focus watchdog: callbackSeen=$focusSeen actualFocus=${host.hasWindowFocus()} shown=${host.isShown} visibility=${host.windowVisibility} size=${host.width}x${host.height}")
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
            android.util.Log.e("SpotlightOverlay", "Overlay window attachment failed", e)
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
                overlayContext,
                onOutside = { close() },
                onFocus = { gained ->
                    if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                        android.util.Log.d("SpotlightOverlay", "Window focus=$gained")
                    }
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
        refreshWindowGeometry()
        view?.let { ViewCompat.requestApplyInsets(it) }
    }

    private fun motionAllowed()=config.animations && runCatching { Settings.Global.getFloat(contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,1f)>0 }.getOrDefault(false)
    private fun close() {
        if(closing) return
        closing=true
        widthTransition.cancel()
        clipboard.removePrimaryClipChangedListener(clipListener)
        showJob?.cancel()
        val host=view
        if(host!=null && host.isAttachedToWindow && motionAllowed()) {
            host.animate().cancel()
            host.animate().alpha(0f).scaleX(.99f).scaleY(.99f).translationY(-3f*resources.displayMetrics.density).setDuration(when(config.movement){"precise"->55L;"liquid_immersive"->115L;else->90L}).withEndAction {if(view===host)finishClose()}.start()
        } else finishClose()
    }
    private fun finishClose() {
        contentExpanded=false
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
        widthTransition.cancel()
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
