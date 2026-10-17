package dev.bautista.spotlight.search

import android.content.Intent
import android.graphics.drawable.Drawable

enum class AiInputOrigin { LEGACY, USER_QUERY, PRIVATE_CONTEXT }

sealed interface Action {
    data class SystemPanel(val page:String,val packageName:String=""):Action
    data class EditClipboard(val text:String):Action
    data class ExpandSnippet(val template:String):Action
    data class TimerForm(val minutes:Int=5):Action
    data class ReadNotification(val key:String):Action
    data class SelectMode(val mode:String,val clearQuery:Boolean=false):Action
    data class WorkflowRun(val id:String):Action
    data class Routine(val minutes: Int, val volume: Int): Action
    data class Shortcut(val packageName: String, val id: String): Action
    data class Navigate(val scope: dev.bautista.spotlight.control.SearchScope): Action
    data class ComposeMessage(val channel:String,val target:String="",val text:String="",val replyKey:String?=null): Action
    data class ComposeSearch(val topic:String,val packageName:String,val query:String=""): Action
    data class AudioPanel(val unused:Unit=Unit): Action
    data class Access(val access:String,val query:String=""): Action
    data class Launch(val intent: Intent) : Action

    data class InApp(val intent: Intent, val fallback: Intent) : Action

    data class Device(val command: String) : Action

    data class RestoreQuery(val query: String): Action

    data class EditNote(val id: String, val text: String): Action

    data class Copy(val text: String) : Action

    data class SaveNote(val text: String) : Action

    data class Ask(val prompt: String,val sensitive:Boolean=false,val messageTarget:String="",val origin:AiInputOrigin=AiInputOrigin.LEGACY,val reviewed:Boolean=false,val continuation:Boolean=false,val priorMessages:List<Pair<String,String>> = emptyList()) : Action

    data class Lookup(val type: String, val query: String) : Action

    data class NewNote(val unused: Unit = Unit) : Action
}

data class Result(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val score: Int,
    val action: Action,
    val icon: Drawable? = null,
    val preview: String? = null,
    val mime: String? = null,
    val parentUri: String? = null,
    val positionMs: Long? = null,
    val sizeBytes:Long?=null, val modifiedAt:Long?=null,
    val matchClass:Int=0,
)

fun normalize(s: String): String =
    java.text.Normalizer.normalize(
            s.lowercase(java.util.Locale.ROOT),
            java.text.Normalizer.Form.NFD,
        )
        .replace(Regex("\\p{M}"), "")

fun relevance(query: String, title: String): Int {
    val q = normalize(query.trim())
    val t = normalize(title)
    if (q.isEmpty()) return 10
    if (q == t) return 1000
    if (t.startsWith(q)) return 800
    if (t.split(' ').any { it.startsWith(q) }) return 650
    if (t.contains(q)) return 500
    // Multiword queries may abbreviate each name token; every query token must match.
    val words=q.split(Regex("[^\\p{L}\\p{N}]+")).filter{it.isNotEmpty()}
    val titles=t.split(Regex("[^\\p{L}\\p{N}]+"))
    if(words.size>1 && words.all{word->titles.any{it.startsWith(word)}})return 620
    if (q.length < 3 || q.length > 128 || t.length > 256) return 0
    val d = distance(q, t)
    return if (d <= 2) 250 - d * 40 else 0
}

private fun distance(a: String, b: String): Int {
    var prev = IntArray(b.length + 1) { it }
    for (i in a.indices) {
        val row = IntArray(b.length + 1)
        row[0] = i + 1
        for (j in b.indices) row[j + 1] =
            minOf(row[j] + 1, prev[j + 1] + 1, prev[j] + if (a[i] == b[j]) 0 else 1)
        prev = row
    }
    return prev.last()
}
