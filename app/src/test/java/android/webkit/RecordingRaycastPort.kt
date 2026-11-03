package android.webkit

import android.os.Handler
import org.json.JSONObject

/** Transport recorder in the Android package solely to access its package-private constructor. */
class RecordingRaycastPort : WebMessagePort() {
    val packets=mutableListOf<JSONObject>()
    var closed=false
    override fun postMessage(message:WebMessage) { packets+=JSONObject(message.data!!) }
    override fun close() { closed=true }
    override fun setWebMessageCallback(callback:WebMessageCallback) { }
    override fun setWebMessageCallback(callback:WebMessageCallback,handler:Handler) { }
}
