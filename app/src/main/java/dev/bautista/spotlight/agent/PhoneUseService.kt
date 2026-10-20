package dev.bautista.spotlight.agent

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayDeque

/**
 * Optional foreground app control. Explicit inspection; never records keystrokes, events
 * or screenshots. Does not control system dialogs or Spotlight's own overlay.
 */
class PhoneUseService : AccessibilityService() {
    companion object {
        @Volatile private var active: PhoneUseService? = null
        fun connected(): Boolean = active != null

        suspend fun inspect(): String = withContext(Dispatchers.Main.immediate) {
            val service = active ?: error("Activá Spotlight · control de aplicaciones en Accesibilidad.")
            service.observe()
        }

        suspend fun tap(expectedPackage: String, selector: String): String = withContext(Dispatchers.Main.immediate) {
            val service = active ?: error("La conexión con Accesibilidad no está disponible.")
            service.operate(expectedPackage, selector, null)
        }

        suspend fun type(expectedPackage: String, selector: String, text: String): String =
            withContext(Dispatchers.Main.immediate) {
                require(PhoneUsePolicy.allowText(text)) { "Texto vacío o demasiado largo." }
                val service = active ?: error("La conexión con Accesibilidad no está disponible.")
                service.operate(expectedPackage, selector, text)
            }
    }

    private var observedPackage = ""
    private var observedSelectors: Set<String> = emptySet()
    private var observedAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        active = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // The overlay redraws when showing tool results or confirming actions.
        // Those updates must not erase a fresh observation of a DIFFERENT app.
        // Any event outside Spotlight invalidates selectors. Unknown packages fail closed.
        if (PhoneUsePolicy.invalidateObservation(event?.packageName?.toString(), applicationContext.packageName, observedPackage)) {
            clearObservation()
        }
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        clearObservation()
        if (active === this) active = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        clearObservation()
        if (active === this) active = null
        super.onDestroy()
    }

    private fun clearObservation() {
        observedPackage = ""
        observedSelectors = emptySet()
        observedAt = 0L
    }

    private fun currentRoot(): Pair<String, AccessibilityNodeInfo> {
        val activeRoot = rootInActiveWindow
        val activePackage = activeRoot?.packageName?.toString().orEmpty()
        val hostPackage = applicationContext.packageName
        if (activeRoot != null && PhoneUsePolicy.allowObservation(activePackage, hostPackage)) {
            return activePackage to activeRoot
        }
        // Do not inspect an app behind a privileged system window (e.g. a permission dialog).
        if (activeRoot != null && activePackage != hostPackage) {
            error("La ventana activa no permite automatización.")
        }
        // Spotlight's own overlay may be focused; only fall back when exactly one
        // external APPLICATION window is accessible. Split-screen ambiguity fails closed.
        val appWindows = windows.orEmpty()
            .filter { it.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION }
            .mapNotNull { it.root }
            .map { it.packageName?.toString().orEmpty() to it }
        check(appWindows.none { (pkg, _) ->
            pkg.isNotBlank() && pkg != hostPackage && !PhoneUsePolicy.allowObservation(pkg, hostPackage)
        }) { "Hay una ventana protegida activa; no se inspeccionará otra app detrás." }
        val candidates = appWindows.filter { PhoneUsePolicy.allowObservation(it.first, hostPackage) }
        check(candidates.size == 1) {
            "No se pudo identificar una única app externa detrás de Spotlight. Abrí la app y reintentá."
        }
        return candidates.single()
    }

    private fun nodes(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val pending = ArrayDeque<AccessibilityNodeInfo>()
        val found = ArrayList<AccessibilityNodeInfo>()
        pending.add(root)
        while (pending.isNotEmpty() && found.size < 220) {
            val node = pending.removeFirst()
            found.add(node)
            for (i in 0 until minOf(node.childCount, 30)) node.getChild(i)?.let(pending::addLast)
        }
        return found
    }

    private fun selectorFor(node: AccessibilityNodeInfo): String? {
        if (!node.isVisibleToUser || node.isPassword || !(node.isClickable || node.isEditable)) return null
        node.viewIdResourceName?.let { id ->
            if (runCatching { PhoneUsePolicy.parseSelector("id:$id") }.isSuccess) return "id:$id"
        }
        if (!node.isEditable) PhoneUsePolicy.compactLabel(node.text)?.let {
            if (it.length >= 2) return "text:$it"
        }
        PhoneUsePolicy.compactLabel(node.contentDescription)?.let {
            if (it.length >= 2) return "desc:$it"
        }
        return null
    }

    private fun observe(): String {
        val (pkg, root) = currentRoot()
        val choices = nodes(root).filter { it.packageName?.toString() == pkg }
            .mapNotNull(::selectorFor).distinct().take(45)
        observedPackage = pkg
        observedSelectors = choices.toSet()
        observedAt = SystemClock.uptimeMillis()
        // Button labels can still be private data; never forward this list to an AI
        // provider automatically, without a separate context-sharing decision.
        return buildString {
            append("App activa: ").append(pkg)
            append("\nControles disponibles (máximo 45):")
            if (choices.isEmpty()) append("\nSin controles accesibles.")
            else choices.forEach { append("\n").append(it) }
            append("\nCoincidencias ambiguas no se ejecutarán.")
        }
    }

    private fun operate(expectedPackage: String, rawSelector: String, input: String?): String {
        val selector = PhoneUsePolicy.parseSelector(rawSelector)
        val (pkg, root) = currentRoot()
        check(PhoneUsePolicy.allowPackage(pkg, expectedPackage, applicationContext.packageName)) {
            "Cambió la aplicación activa. No se realizó ninguna acción."
        }
        check(observedPackage == pkg && rawSelector in observedSelectors &&
            SystemClock.uptimeMillis() - observedAt in 0L..20000L) {
            "El control no fue observado recientemente. Inspeccioná la pantalla antes de actuar."
        }
        clearObservation() // One observation permits at most one action.
        val matches = nodes(root).filter { node ->
            if (node.packageName?.toString() != pkg || node.isPassword || !node.isVisibleToUser) false
            else {
                val matchesSelector = when (selector) {
                    is PhoneUsePolicy.Selector.Id -> node.viewIdResourceName == selector.value
                    is PhoneUsePolicy.Selector.Label -> node.text?.toString() == selector.value
                    is PhoneUsePolicy.Selector.Description -> node.contentDescription?.toString() == selector.value
                }
                matchesSelector && (if (input == null) node.isClickable else node.isEditable)
            }
        }
        check(matches.size == 1) {
            if (matches.isEmpty()) "No se encontró un control único y compatible."
            else "El selector coincide con varios controles. No se tocó ninguno."
        }
        val target = matches.single()
        val accepted = if (input == null) {
            target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, input)
            })
        }
        check(accepted) { "Android rechazó la acción. No se intentará de nuevo automáticamente." }
        return if (input == null) "Android aceptó la pulsación en $pkg. Inspeccioná para verificar el resultado."
        else "Android aceptó introducir texto en $pkg. Inspeccioná antes de continuar."
    }
}
