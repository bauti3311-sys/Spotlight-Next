package dev.bautista.spotlight.advanced

enum class ConnectionStatus(val label:String) {
    ABSENT("No instalado"), STOPPED("Servicio detenido"), AUTHORIZATION("Pendiente de autorización"), DENIED("Autorización denegada"), CONNECTED("Conectado"), DISCONNECTED("Servicio desconectado"), LIMITED("Compatibilidad limitada")
}
data class AdvancedState(val status:ConnectionStatus=ConnectionStatus.STOPPED,val version:Int?=null,val uid:Int?=null,val authorized:Boolean=false) {
    val available:Boolean get()=authorized && status==ConnectionStatus.CONNECTED && uid==2000
    val privilege:String get()=when(uid){2000->"ADB / shell";0->"Root · operaciones avanzadas deshabilitadas";null->"Sin conexión";else->"Identidad no compatible"}
}
object AdvancedPolicy {
    val settingKeys=setOf("screen_brightness","screen_off_timeout","accelerometer_rotation")
    fun validSetting(key:String,value:String)=when(key){"screen_brightness"->value.toIntOrNull() in 20..255;"screen_off_timeout"->value.toLongOrNull() in 30000L..1800000L;"accelerometer_rotation"->value in setOf("0","1");else->false}
    fun readableSetting(key:String)=key in settingKeys
    fun canStop(packageName:String,system:Boolean,protected:Set<String>):Boolean = !system && packageName.matches(Regex("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)+")) && packageName !in protected && !packageName.startsWith("com.android.") && !packageName.startsWith("com.google.android.permissioncontroller") && !packageName.startsWith("dev.bautista.spotlight")
    fun state(installed:Boolean,alive:Boolean,granted:Boolean,denied:Boolean,version:Int?,uid:Int?,previouslyConnected:Boolean=false)=AdvancedState(
        when{!installed && !alive->ConnectionStatus.ABSENT;!alive->if(previouslyConnected)ConnectionStatus.DISCONNECTED else ConnectionStatus.STOPPED;version==null || version<13->ConnectionStatus.LIMITED;!granted->if(denied)ConnectionStatus.DENIED else ConnectionStatus.AUTHORIZATION;uid!=2000->ConnectionStatus.LIMITED;else->ConnectionStatus.CONNECTED},version,uid,granted)
}
