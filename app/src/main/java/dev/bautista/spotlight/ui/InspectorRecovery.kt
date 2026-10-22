package dev.bautista.spotlight.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.saveable.Saver
import dev.bautista.spotlight.search.*
import org.json.*

/** Descriptors only: never persist a live Intent, grant, renderer, icon or extracted document body. */
internal val DocumentPreviewSaver=Saver<Result?,String>(save={item ->
    val uri=(item?.action as? Action.Launch)?.intent?.data
    if(item==null || item.category!="Archivos" || uri?.scheme!="content")null
    else JSONObject().put("id",item.id).put("title",item.title).put("subtitle",item.subtitle)
        .put("uri",uri.toString()).put("mime",item.mime).put("modified",item.modifiedAt).put("size",item.sizeBytes).toString().takeIf{it.length<=8000}
},restore={raw -> runCatching {
    require(raw.length<=8000);val j=JSONObject(raw);val uri=Uri.parse(j.getString("uri"));require(uri.scheme=="content" && !uri.authority.isNullOrBlank())
    Result(j.getString("id"),j.getString("title"),j.getString("subtitle"),"Archivos",0,
        Action.Launch(Intent(Intent.ACTION_VIEW).setDataAndType(uri,j.optString("mime").takeIf{it.isNotBlank()})),
        mime=j.optString("mime").takeIf{it.isNotBlank()},modifiedAt=if(j.isNull("modified"))null else j.getLong("modified"),sizeBytes=if(j.isNull("size"))null else j.getLong("size"))
}.getOrNull()})

internal val InfoCardSaver=Saver<InfoCard?,String>(save={c -> c?.let {
    JSONObject().put("title",it.title).put("subtitle",it.subtitle.removePrefix("Recuperada · ")).put("body",it.body)
        .put("copy",it.copyText).put("url",it.url).put("rows",JSONArray(it.rows.take(24).map{p->listOf(p.first,p.second)})).toString().takeIf{raw->raw.length<=64_000}
}},restore={raw -> runCatching {
    require(raw.length<=64_000);val j=JSONObject(raw);val rows=j.getJSONArray("rows");require(rows.length()<=24)
    val url=if(j.isNull("url"))null else j.getString("url").takeIf{Uri.parse(it).scheme in setOf("https","http")}
    InfoCard(j.getString("title"),"Recuperada · "+j.getString("subtitle"),j.getString("body"),j.getString("copy"),url,(0 until rows.length()).map{val row=rows.getJSONArray(it);row.getString(0) to row.getString(1)})
}.getOrNull()})

internal val PreviewPagesSaver=Saver<SnapshotStateMap<String,Int>,String>(save={pages ->
    JSONObject().apply{pages.entries.toList().takeLast(8).forEach{put(it.key,it.value)}}.toString()
},restore={raw ->runCatching {
    require(raw.length<=8000);val j=JSONObject(raw);require(j.length()<=8)
    mutableStateMapOf<String,Int>().apply{j.keys().forEach{key->require(key.length<=800);put(key,j.getInt(key).also{require(it in 0..1_000_000)})}}
}.getOrDefault(mutableStateMapOf())})
