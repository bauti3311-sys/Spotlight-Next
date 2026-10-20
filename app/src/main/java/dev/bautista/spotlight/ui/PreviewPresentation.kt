package dev.bautista.spotlight.ui

import android.provider.DocumentsContract
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result

/** Only content that adds information earns a second pane. Actions never do. */
object PreviewPresentation {
    /**
     * The home feed stores a single real application as "Sugerencias" and
     * uses suggest:app:PACKAGE; it must get the same native inspector as an
     * application in search results, never an inert/imaginary inspector.
     */
    fun isApplication(result:Result?):Boolean = result!=null && (
        result.category=="Aplicaciones" ||
        result.id.startsWith("suggest:app:") && result.action is Action.Launch
    )

    fun needsInspector(result:Result?):Boolean = result!=null && (
        result.category=="Archivos" && result.mime!=DocumentsContract.Document.MIME_TYPE_DIR ||
        result.action is Action.ReadNotification ||
        result.category in setOf("Notas","Portapapeles") && (result.preview?.length ?: 0)>160)
}
