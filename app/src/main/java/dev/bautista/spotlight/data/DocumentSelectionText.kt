package dev.bautista.spotlight.data

import dev.bautista.spotlight.index.ExtractedDocument

/** Bounded, attributable text shown verbatim in the existing private-context review. */
internal object DocumentSelectionText {
    fun excerpt(id:String,title:String,document:ExtractedDocument,limit:Int):String {
        require(limit>=1000)
        val name=title.replace(Regex("[\\p{Cntrl}]")," ").take(120)
        val hash=document.sourceSha256.takeIf{it.matches(Regex("[0-9a-f]{64}"))} ?: "no disponible; revisar fuente"
        val header="[$id] Documento: $name\nSHA-256: $hash\n${document.coverage}\n"
        val footer="\n[Fin $id · extracto acotado; no representa todo el documento.]"
        val clipped="\n[Recorte adicional por presupuesto de selección.]"
        val room=limit-header.length-footer.length-clipped.length
        require(room>0)
        val content=StringBuilder();var omitted=false
        if(document.pages>0) {
            require(document.fragments.size<=40){"Procedencia PDF inválida."}
            for(f in document.fragments) {
                require(f.page in 1..document.pages && f.method in setOf("pdf_text","ocr_local") && f.start>=0 && f.end>f.start && f.end<=document.text.length){"Procedencia PDF inválida."}
                val label="[$id · página ${f.page} · ${if(f.method=="ocr_local")"OCR local (revisar)" else "texto PDF"}]\n"
                val remaining=room-content.length-label.length-1
                if(remaining<=0){omitted=true;break}
                val part=document.text.substring(f.start,f.end)
                content.append(label).append(part.take(remaining)).append('\n')
                if(part.length>remaining){omitted=true;break}
            }
        } else {
            content.append(document.text.take(room));omitted=document.text.length>room
        }
        check(content.isNotBlank()){"No se obtuvo texto compatible de $name."}
        // Each document has its own complete header/footer; no final take() can hide D12.
        return header+content.toString().trimEnd()+(if(omitted)clipped else "")+footer
    }
}
