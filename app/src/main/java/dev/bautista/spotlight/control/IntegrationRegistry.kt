package dev.bautista.spotlight.control

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.os.Process
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class CapabilityState { AVAILABLE, PERMISSION_REQUIRED, UNSUPPORTED }
data class Capability(val title: String, val state: CapabilityState, val explanation: String)

/** Android is the authority. Only exported, resolvable activities and public
 * shortcuts are exposed; no inferred private app automation. */
class IntegrationRegistry(private val context: Context) {
    fun resolves(intent: Intent): Boolean = runCatching {
        val activity=context.packageManager.resolveActivity(intent,0)?.activityInfo ?: return@runCatching false
        if(intent.`package`!=null && activity.packageName!=intent.`package`)return@runCatching false
        activity.enabled && activity.applicationInfo?.let { it.enabled && (it.flags and ApplicationInfo.FLAG_SUSPENDED)==0 }!=false &&
            (activity.packageName==context.packageName || activity.exported) &&
            (activity.permission.isNullOrBlank() || androidx.core.content.ContextCompat.checkSelfPermission(context,activity.permission)==android.content.pm.PackageManager.PERMISSION_GRANTED)
    }.getOrDefault(false)
    internal data class LaunchAccess(val component:android.content.ComponentName?=null,val label:String="",val problem:LaunchProblem?=null)
    /** Live resolution for dispatch, using the same exported/enabled/permission policy as discovery. */
    internal fun launchAccess(intent:Intent):LaunchAccess = try {
        val pm=context.packageManager
        val resolved=pm.resolveActivity(intent,android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS)?.activityInfo
        if(resolved==null)LaunchAccess(problem=LaunchProblem.UNAVAILABLE) else {
            val component=android.content.ComponentName(resolved.packageName,resolved.name)
            val activity=pm.getActivityInfo(component,android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS)
            val app=pm.getApplicationInfo(component.packageName,android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS)
            when {
                intent.component?.let{it!=component}==true || intent.`package`?.let{it!=component.packageName}==true -> LaunchAccess(problem=LaunchProblem.UNAVAILABLE)
                !activity.enabled || !app.enabled || app.flags and ApplicationInfo.FLAG_SUSPENDED!=0 -> LaunchAccess(problem=LaunchProblem.UNAVAILABLE)
                component.packageName!=context.packageName && !activity.exported -> LaunchAccess(problem=LaunchProblem.PERMISSION_REQUIRED)
                !activity.permission.isNullOrBlank() && component.packageName!=context.packageName &&
                    androidx.core.content.ContextCompat.checkSelfPermission(context,activity.permission)!=android.content.pm.PackageManager.PERMISSION_GRANTED -> LaunchAccess(problem=LaunchProblem.PERMISSION_REQUIRED)
                !grantsAvailable(intent) -> LaunchAccess(problem=LaunchProblem.PERMISSION_REQUIRED)
                else -> LaunchAccess(component,"${activity.loadLabel(pm).toString().take(120)} · ${component.packageName}")
            }
        }
    }catch(_:SecurityException){LaunchAccess(problem=LaunchProblem.PERMISSION_REQUIRED)}
     catch(_:Exception){LaunchAccess(problem=LaunchProblem.UNAVAILABLE)}
    private fun grantsAvailable(intent:Intent):Boolean {
        val modes=listOf(Intent.FLAG_GRANT_READ_URI_PERMISSION,Intent.FLAG_GRANT_WRITE_URI_PERMISSION).filter{intent.flags and it!=0}
        if(modes.isEmpty())return true
        val uris=buildList<android.net.Uri> {
            intent.data?.let(::add)
            intent.clipData?.let{clip->repeat(clip.itemCount){clip.getItemAt(it).uri?.let(::add)}}
            @Suppress("DEPRECATION")
            (intent.getParcelableExtra<android.os.Parcelable>(Intent.EXTRA_STREAM) as? android.net.Uri)?.let(::add)
        }
        return uris.filter{it.scheme=="content"}.all { uri -> modes.all { mode ->
            context.checkUriPermission(uri,android.os.Process.myPid(),android.os.Process.myUid(),mode)==android.content.pm.PackageManager.PERMISSION_GRANTED
        } }
    }
    fun capabilities(packageName: String): List<Capability> {
        val launcher = context.getSystemService(LauncherApps::class.java)
        val base=listOf(
            Capability("Abrir", if(context.packageManager.getLaunchIntentForPackage(packageName) != null) CapabilityState.AVAILABLE else CapabilityState.UNSUPPORTED, "Actividad de inicio de Android"),
            Capability("Buscar dentro de la app", if(AppCapabilities(context).available(packageName).any{it.id=="search"}) CapabilityState.AVAILABLE else CapabilityState.UNSUPPORTED, "Actividad pública de búsqueda"),
            Capability("Accesos directos publicados", if(launcher?.hasShortcutHostPermission() == true) CapabilityState.AVAILABLE else CapabilityState.UNSUPPORTED, "Android los reserva al lanzador predeterminado; Spotlight conserva tu lanzador actual")
        )
        val documented=if(AppCapabilities(context).available(packageName).any{it.mechanism==AppCapabilityMechanism.DOCUMENTED_LINK} && packageName in setOf("com.spotify.music","com.google.android.youtube","com.google.android.apps.maps","com.sofascore.results"))listOf(Capability("Búsqueda documentada",CapabilityState.AVAILABLE,"Abrir búsqueda mediante enlace compatible o respaldo web; no controlar datos internos")) else emptyList()
        return base + documented + if(packageName in setOf("com.whatsapp","com.whatsapp.w4b")) listOf(
            Capability("Elegir contactos",if(People.allowed(context)) CapabilityState.AVAILABLE else CapabilityState.PERMISSION_REQUIRED,"READ_CONTACTS se solicita al elegir un nombre"),
            Capability("Responder notificaciones",if(Replies.connected.value) CapabilityState.AVAILABLE else CapabilityState.PERMISSION_REQUIRED,"Acceso especial a notificaciones; solo respuestas que la app publica")
        ) else emptyList()
    }
    suspend fun menu(topic: String, launch: Intent?, packageName: String?): List<Result> = withContext(Dispatchers.IO) {
        val icon = packageName?.let { runCatching { context.packageManager.getApplicationIcon(it) }.getOrNull() }
        val installed = launch != null
        val rows = ActionCatalog.menu(topic, launch, packageName).filter { row ->
            when(val action = row.action) {
                is Action.ComposeMessage -> action.channel !in setOf("whatsapp","whatsapp_business","telegram") || installed
                is Action.ComposeSearch -> topic in setOf("youtube", "spotify", "mapas", "google", "chrome", "vivaldi", "sofascore") || packageName != null && resolves(Intent(Intent.ACTION_SEARCH).setPackage(packageName))
                is Action.Navigate -> action.scope.section!="replies" || installed
                is Action.Launch -> resolves(action.intent)
                else -> true
            }
        }.map { row ->
            if(row.id.endsWith(":open")) row.copy(id=launch?.component?.let { "${it.packageName}/${it.className}" } ?: row.id, category="Aplicaciones", icon=icon)
            else if(row.action is Action.ComposeSearch && !installed) row.copy(subtitle="Búsqueda web disponible · app no instalada")
            else row
        }.toMutableList()
        if(packageName!=null && installed && topic !in setOf("whatsapp","whatsapp_business","telegram","gmail")) {
            val share=Intent(Intent.ACTION_SEND).setType("text/plain").setPackage(packageName)
            if(resolves(share)) rows.add(1,Result("share:$packageName","Compartir texto en $topic","Elegir destino dentro de la app","Acciones",2150,Action.ComposeMessage("share:$packageName",topic)))
        }
        if(installed && packageName!=null)rows += Result("inspector:$packageName","Ver permisos y componentes","Información local de la aplicación","Sistema",2150,Action.SystemPanel("app",packageName))
        val launcher = context.getSystemService(LauncherApps::class.java)
        if(packageName != null && launcher?.hasShortcutHostPermission() == true) {
            runCatching {
                launcher.getShortcuts(LauncherApps.ShortcutQuery().setPackage(packageName).setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED), Process.myUserHandle()).orEmpty()
                    .filter { it.isEnabled }.take(12).forEach { shortcut ->
                        rows += Result("shortcut:$packageName:${shortcut.id}", shortcut.shortLabel?.toString() ?: "Acceso directo", "Publicado por $topic", "Acciones", 2100, Action.Shortcut(packageName, shortcut.id), runCatching { launcher.getShortcutIconDrawable(shortcut, 0) }.getOrNull())
                    }
            }
        }
        rows
    }
}
