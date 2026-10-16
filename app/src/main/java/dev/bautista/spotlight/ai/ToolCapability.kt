package dev.bautista.spotlight.ai

enum class CapabilityStatus(val label:String) {
 AVAILABLE("Disponible"), DISABLED("Desactivada"), PERMISSION_REQUIRED("Permiso pendiente"),
 CONNECTION_REQUIRED("Conexión pendiente"), UNSUPPORTED("No disponible")
}
data class ToolCapability(val definition:ToolDefinition,val status:CapabilityStatus,val reason:String)
