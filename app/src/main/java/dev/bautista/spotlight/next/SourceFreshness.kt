package dev.bautista.spotlight.next

/** fetchedAt is the local retrieval time, not an asserted provider update time. */
data class SourceFreshness(val provider:String,val fetchedAt:Long,val fromCache:Boolean,val stale:Boolean) {
    fun knownTimestamp(now:Long)=fetchedAt.takeIf{it>0 && it<=now}
    val status get()=if(stale)"Sin actualizar · copia guardada" else if(fromCache)"Copia guardada" else "Consulta recibida"
}
