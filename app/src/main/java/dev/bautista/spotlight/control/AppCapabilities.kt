package dev.bautista.spotlight.control

import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.bautista.spotlight.search.Action

/** Availability describes an Android entry point, never account access or a completed effect. */
enum class AppCapabilityMechanism { PUBLIC_SEARCH, PUBLIC_WEB_SEARCH, DOCUMENTED_LINK, SHARE_INTENT }
data class AppCapability(
    val id:String,val packageName:String,val title:String,val mechanism:AppCapabilityMechanism,
    val parameters:List<String>,val confirmationRequired:Boolean,val permissions:List<String>,
    val expectedResult:String,val fallback:String,val action:Action,
)
class AppCapabilities(private val context:Context) {
    private val registry=IntegrationRegistry(context)
    private fun probeSearch(pkg:String)=Intent(Intent.ACTION_SEARCH).setPackage(pkg).putExtra(android.app.SearchManager.QUERY,"consulta")
    private fun share(pkg:String)=Intent(Intent.ACTION_SEND).setType("text/plain").setPackage(pkg)
    private val searches=mapOf(
        "com.spotify.music" to ("spotify" to "https://open.spotify.com/search/consulta"),
        "com.google.android.youtube" to ("youtube" to "https://www.youtube.com/results?search_query=consulta"),
        "com.google.android.apps.maps" to ("mapas" to "https://www.google.com/maps/search/?api=1&query=consulta"),
        "com.android.chrome" to ("chrome" to "https://www.google.com/search?q=consulta"),
        "com.vivaldi.browser" to ("vivaldi" to "https://www.google.com/search?q=consulta"),
    )
    fun available(packageName:String):List<AppCapability> {
        require(packageName.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")))
        val rows=mutableListOf<AppCapability>()
        val documented=searches[packageName]
        if(documented!=null && registry.resolves(Intent(Intent.ACTION_VIEW,Uri.parse(documented.second)).setPackage(packageName))) {
            rows+=AppCapability("search",packageName,"Buscar en la aplicación",AppCapabilityMechanism.DOCUMENTED_LINK,
                listOf("query"),true,emptyList(),"Abrir resultados en la app; no reproduce ni consulta una cuenta desde Spotlight.","Alternativa web sólo tras revisión y confirmación explícitas.",Action.ComposeSearch(documented.first,packageName))
        } else if(registry.resolves(probeSearch(packageName))) {
            rows+=AppCapability("search",packageName,"Buscar en la aplicación",AppCapabilityMechanism.PUBLIC_SEARCH,
                listOf("query"),true,emptyList(),"Solicitar búsqueda a una actividad pública.","Sin acceso a los datos privados de la app.",Action.ComposeSearch("app",packageName))
        }
        if(rows.none{it.id=="search"} && registry.resolves(Intent(Intent.ACTION_WEB_SEARCH).setPackage(packageName))) {
            rows+=AppCapability("search",packageName,"Buscar en la aplicación",AppCapabilityMechanism.PUBLIC_WEB_SEARCH,
                listOf("query"),true,emptyList(),"Solicitar búsqueda web a la app seleccionada; no leer sus datos privados.",
                "Sin cambio automático de aplicación.",Action.ComposeSearch("app-web-search",packageName))
        }
        if(registry.resolves(share(packageName))) {
            val title=when(packageName){"com.whatsapp","com.whatsapp.w4b"->"Preparar mensaje";"com.google.android.gm"->"Redactar correo";else->"Compartir texto"}
            val label=runCatching{context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(packageName,0)).toString()}.getOrDefault(packageName)
            rows+=AppCapability("share-text",packageName,title,AppCapabilityMechanism.SHARE_INTENT,
                listOf("text"),true,emptyList(),"Preparar contenido; destinatario y envío final se revisan en la app.","Si deja de estar disponible, volver a revisar; no se elige otra app automáticamente.",Action.ComposeMessage("share:$packageName",label))
        }
        return rows
    }
    /** Re-resolve immediately before opening a form; no launch, send or network call here. */
    fun prepare(packageName:String,id:String):Action = available(packageName).singleOrNull{it.id==id}?.action
        ?: error("La capacidad ya no está disponible o su permiso cambió.")
}
