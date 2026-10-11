package dev.bautista.spotlight.advanced

import dev.bautista.spotlight.search.*
object AdvancedCommands {
    private val commands=listOf(
        Triple("device","Información del dispositivo","Modelo, batería, memoria y pantalla"),
        Triple("shizuku","Diagnóstico de Shizuku","Conexión, autorización y capacidades"),
        Triple("controls","Sistema y controles avanzados","Funciones públicas y operaciones opcionales"))
    fun results(query:String,all:Boolean=false):List<Result> {
        val q=normalize(query.trim())
        val preference=when(q){"cambiar tema","ajustar transparencia","apariencia","configurar apariencia"->"preferences:appearance";"configurar ia","configurar inteligencia artificial"->"preferences:ai";"cambiar atajo","configurar atajos"->"preferences:shortcuts";"administrar sugerencias","configurar sugerencias"->"preferences:suggestions";"abrir permisos","permisos de spotlight"->"preferences:permissions";else->null}
        if(preference!=null)return listOf(Result("preference:$preference",query.trim().replaceFirstChar{it.uppercase()},"Ajuste de Spotlight · dentro de la barra","Ajustes",2450,Action.SystemPanel(preference)))
        if(q in setOf("pomodoro","concentracion","modo concentracion","focus","sesion de estudio"))return listOf(Result("focus:panel","Concentración","Pomodoro, pausas y herramientas de estudio","Acciones",2450,Action.SystemPanel("focus")))
        if(q in setOf("ia con herramientas","preparar acciones con ia","automatizar con ia"))return listOf(Result("ai:tools","IA con herramientas","Propuestas tipadas y revisión por acción","IA",2450,Action.SystemPanel("agent")))
        return commands.filter{all || relevance(q,it.second)>0 || q in when(it.first){"device"->setOf("dispositivo","diagnostico del dispositivo","memoria","densidad de pantalla","consultar densidad de pantalla");"shizuku"->setOf("shizuku","configurar shizuku","diagnostico de shizuku","consultar funciones avanzadas");else->setOf("controles avanzados","mostrar controles avanzados","sistema y controles avanzados","restaurar ajustes","restaurar un ajuste","restaurar pantalla")}}.map{(id,title,detail)->Result("advanced:$id",title,detail,"Sistema",2350,Action.SystemPanel(id))}
    }
}
