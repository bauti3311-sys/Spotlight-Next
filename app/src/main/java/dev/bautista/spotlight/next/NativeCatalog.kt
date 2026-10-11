package dev.bautista.spotlight.next

import android.content.Intent
import android.provider.Settings
import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.automation.*

data class CatalogItem(val id:String,val title:String,val detail:String,val pack:String,val action:Action,val requirement:String="Disponible")
object NativeCatalog {
    private fun setting(id:String,title:String,intent:String)=CatalogItem(id,title,"Abrir el panel de Android","Sistema",Action.Launch(Intent(intent)))
    val items=listOf(
        CatalogItem("study","Modo estudio","Volumen 15% y temporizador de 45 minutos; revisar antes de activar","Estudio",Action.SystemPanel("profiles","estudio")),
        CatalogItem("read","Modo lectura","Volumen 0% y temporizador de 30 minutos; revisar antes de activar","Estudio",Action.SystemPanel("profiles","lectura")),
        CatalogItem("focus","Modo concentración","Volumen 10% y temporizador de 25 minutos; revisar antes de activar","Productividad",Action.SystemPanel("profiles","concentracion")),
        CatalogItem("media","Modo multimedia","Ajustar volumen multimedia al 60%; revisar antes de activar","Multimedia",Action.SystemPanel("profiles","multimedia")),
        CatalogItem("session","Concentración y descansos","Sesión local, pausas y estadísticas","Productividad",Action.SystemPanel("focus")),
        CatalogItem("note","Nueva nota","Escribir y guardar una nota local","Productividad",Action.NewNote()),
        CatalogItem("timer","Temporizador","Revisar duración y abrir el reloj compatible","Productividad",Action.TimerForm(25)),
        CatalogItem("wa","Mensaje de WhatsApp","Elegir contacto y revisar el contenido","Comunicación",Action.ComposeMessage("whatsapp")),
        CatalogItem("contacts","Contactos","Buscar personas sin mezclar archivos","Comunicación",Action.Navigate(SearchScope("","contacts")),"Requiere permiso de contactos"),
        CatalogItem("youtube","Buscar en YouTube","Buscar contenido en la app o en la web","Multimedia",Action.ComposeSearch("youtube","com.google.android.youtube")),
        CatalogItem("spotify","Buscar en Spotify","Elegir artista, canción o lista","Multimedia",Action.ComposeSearch("spotify","com.spotify.music")),
        CatalogItem("google","Buscar en Google","Búsqueda web preconfigurada","Productividad",Action.ComposeSearch("google","")),
        CatalogItem("files","Archivos y carpetas","Entrar al buscador de archivos autorizado","Productividad",Action.SelectMode("Archivos",true)),
        CatalogItem("ai","Herramientas de IA","Consultas y acciones con revisión","IA",Action.SystemPanel("agent"),"Proveedor IA configurado"),
        CatalogItem("weather","Clima y pronóstico","Open-Meteo directo, sin clave ni backend","Clima y deportes",Action.SystemPanel("weather-native")),
        CatalogItem("sports","Agenda deportiva","Resultados reportados por TheSportsDB; cobertura gratuita limitada","Clima y deportes",Action.SystemPanel("sports-native")),
        CatalogItem("tasker","Tareas de Tasker","Descubrir tareas, pasar parámetros y esperar confirmación","Automatización",Action.SystemPanel("tasker"),"Tasker y acceso externo"),
        CatalogItem("device","Información del dispositivo","Batería, memoria y almacenamiento reales","Sistema",Action.SystemPanel("device")),
        CatalogItem("shizuku","Conectar Shizuku","Estado, autorización y diagnóstico","Shizuku",Action.SystemPanel("shizuku")),
        CatalogItem("restore","Controles y restauración","Cambios reversibles en brillo, rotación y suspensión","Shizuku",Action.SystemPanel("controls"),"Shizuku ADB autorizado"),
        CatalogItem("brightness","Brillo al 40%","Revisar el cambio y conservar el valor original","Shizuku",Action.SystemPanel("display-review","screen_brightness:102"),"Shizuku ADB autorizado"),
        CatalogItem("rotation","Desactivar giro automático","Cambio reversible con confirmación","Shizuku",Action.SystemPanel("display-review","accelerometer_rotation:0"),"Shizuku ADB autorizado"),
        CatalogItem("timeout","Pantalla encendida 10 minutos","Cambiar suspensión con restauración","Shizuku",Action.SystemPanel("display-review","screen_off_timeout:600000"),"Shizuku ADB autorizado"),
        setting("wifi","Ajustes de Wi-Fi",Settings.ACTION_WIFI_SETTINGS),setting("bt","Ajustes de Bluetooth",Settings.ACTION_BLUETOOTH_SETTINGS),
        setting("sound","Ajustes de sonido",Settings.ACTION_SOUND_SETTINGS),setting("display","Ajustes de pantalla",Settings.ACTION_DISPLAY_SETTINGS),
        setting("battery","Ahorro de batería",Settings.ACTION_BATTERY_SAVER_SETTINGS),setting("storage","Almacenamiento",Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
        setting("location","Ajustes de ubicación",Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    )
    fun profile(name:String):Workflow?=when(name){
        "estudio"->Workflow("native:study","Modo estudio",steps=listOf(WorkflowStep("volume","15"),WorkflowStep("timer","45")))
        "lectura"->Workflow("native:reading","Modo lectura",steps=listOf(WorkflowStep("volume","0"),WorkflowStep("timer","30")))
        "concentracion"->Workflow("native:focus","Modo concentración",steps=listOf(WorkflowStep("volume","10"),WorkflowStep("timer","25")))
        "multimedia"->Workflow("native:media","Modo multimedia",steps=listOf(WorkflowStep("volume","60")))
        else->null
    }
    fun results(query:String,context:android.content.Context?=null):List<Result> {
        val disabled=context?.getSharedPreferences("catalog_v1",android.content.Context.MODE_PRIVATE)?.getStringSet("disabled",emptySet()).orEmpty()
        val q=normalize(query.trim());return items.filter{it.id !in disabled && (q.isBlank() || relevance(q,it.title)>0 || q==normalize(it.pack))}.map {Result("native:catalog:${it.id}",it.title,it.detail,"Comandos",1450,it.action)}
    }
}
