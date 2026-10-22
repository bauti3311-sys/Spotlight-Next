package dev.bautista.spotlight.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.saveable.Saver
import dev.bautista.spotlight.search.*
import org.json.*

/** Descriptors only: never persist a live Intent, grant, renderer, icon or extracted document body. */
internal val DocumentPreviewSaver=Saver<Result?,String>(save=save@{item ->
    if(item?.action is Action.EditNote)return@save JSONObject().put("kind","note").put("id",item.action.id).toString()
    val uri=(item?.action as? Action.Launch)?.intent?.data
    if(item==null || item.category!="Archivos" || uri?.scheme!="content")null
    else JSONObject().put("id",item.id).put("title",item.title).put("subtitle",item.subtitle)
        .put("uri",uri.toString()).put("mime",item.mime).put("modified",item.modifiedAt).put("size",item.sizeBytes).toString().takeIf{it.length<=8000}
},restore={raw -> runCatching {
    require(raw.length<=8000);val j=JSONObject(raw)
    if(j.optString("kind")=="note")return@runCatching Result("note:"+j.getString("id"),"Nota local","Revalidando nota guardada","Notas",0,Action.EditNote(j.getString("id"),""))
    val uri=Uri.parse(j.getString("uri"));require(uri.scheme=="content" && !uri.authority.isNullOrBlank())
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

internal val SearchScopeSaver=Saver<dev.bautista.spotlight.control.SearchScope?,String>(save={it?.let{v->JSONArray(listOf(v.topic,v.section,v.kind)).toString()}},restore={raw->runCatching{
    require(raw.length<=16000);val a=JSONArray(raw);require(a.length()==3)
    dev.bautista.spotlight.control.SearchScope(a.getString(0),a.getString(1),a.getString(2))
}.getOrNull()})

/** Application identity is resolved anew; cached launch flags/components are not authorizations. */
internal fun inspectorResultSaver(context:android.content.Context)=Saver<Result?,String>(save={item->
    val owner=this
    if(PreviewPresentation.isApplication(item)) {
        (item?.action as? Action.Launch)?.intent?.component?.packageName?.let{JSONObject().put("kind","app").put("package",it).toString()}
    } else with(DocumentPreviewSaver){owner.save(item)}
},restore={raw->runCatching {
    require(raw.length<=8000)
    val j=JSONObject(raw)
    if(j.optString("kind")=="app") {
        val pkg=j.getString("package");require(pkg.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")))
        val intent=context.packageManager.getLaunchIntentForPackage(pkg) ?: return@runCatching null
        val info=context.packageManager.getApplicationInfo(pkg,0)
        Result("$pkg/${intent.component?.className}",context.packageManager.getApplicationLabel(info).toString(),"Aplicación instalada","Aplicaciones",0,Action.Launch(intent),runCatching{context.packageManager.getApplicationIcon(info)}.getOrNull())
    } else DocumentPreviewSaver.restore(raw)
}.getOrNull()})

internal val FileBufferSaver=Saver<dev.bautista.spotlight.data.FileBuffer,String>(save={buffer->
    val owner=this
    JSONArray(buffer.items.mapNotNull{with(DocumentPreviewSaver){owner.save(it)}}).toString()
},restore={raw->runCatching{
    require(raw.length<=96000);val a=JSONArray(raw);require(a.length()<=12)
    dev.bautista.spotlight.data.FileBuffer().apply{for(i in 0 until a.length())DocumentPreviewSaver.restore(a.getString(i))?.let{add(it)}}
}.getOrDefault(dev.bautista.spotlight.data.FileBuffer())})
