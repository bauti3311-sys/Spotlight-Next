package dev.bautista.spotlight.ui

import android.provider.DocumentsContract
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result

/** Only content that adds information earns a second pane. Actions never do. */
object PreviewPresentation {
    fun needsInspector(result:Result?):Boolean = result!=null && (
        result.category=="Archivos" && result.mime!=DocumentsContract.Document.MIME_TYPE_DIR ||
        result.action is Action.ReadNotification ||
        result.category in setOf("Notas","Portapapeles") && (result.preview?.length ?: 0)>160)
}
