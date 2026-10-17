package dev.bautista.spotlight

import android.app.Application
import dev.bautista.spotlight.data.Preferences
import dev.bautista.spotlight.search.SearchEngine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class SpotlightApp : Application() {
    val processStartedAt=android.os.SystemClock.elapsedRealtime()
    val scope =
        kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO
        )

    override fun onCreate() {
        super.onCreate()
        scope.launch { preferences.config.collectLatest { } }
        contexts.start()
        scope.launch { dev.bautista.spotlight.index.PdfSnapshotFiles.removeAbandoned(this@SpotlightApp) }
        val filter =
            android.content.IntentFilter().apply {
                addAction(android.content.Intent.ACTION_PACKAGE_ADDED)
                addAction(android.content.Intent.ACTION_PACKAGE_REMOVED)
                addAction(android.content.Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
        androidx.core.content.ContextCompat.registerReceiver(
            this,
            object : android.content.BroadcastReceiver() {
                override fun onReceive(
                    context: android.content.Context?,
                    intent: android.content.Intent?,
                ) {
                    search.invalidateApps()
                }
            },
            filter,
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    val shizuku by lazy { dev.bautista.spotlight.advanced.ShizukuManager(this) }
    val advancedBackend by lazy {dev.bautista.spotlight.advanced.ShizukuBackend(this,shizuku)}
    val settingsJournal by lazy {dev.bautista.spotlight.advanced.SettingsJournal(this)}
    val reversibleSettings by lazy {dev.bautista.spotlight.advanced.ReversibleSettings(advancedBackend,settingsJournal)}
    val toolAudit by lazy {dev.bautista.spotlight.ai.ToolAudit(this)}
    val preferences by lazy { Preferences(this) }
    val contexts by lazy { dev.bautista.spotlight.data.ContextStore(this,scope,preferences) }
    val diagnostics by lazy { dev.bautista.spotlight.overlay.Diagnostics(this) }
    val local by lazy { dev.bautista.spotlight.data.LocalStore(preferences, scope) }
    val index by lazy { dev.bautista.spotlight.index.FileIndex(this,liveConfig={preferences.current?.let{it.copy(privateMode=it.privateMode || preferences.isPrivate)}}) }
    val online by lazy { dev.bautista.spotlight.search.OnlineTools() }
    val search by lazy { SearchEngine(this, preferences, local, index) }
}
