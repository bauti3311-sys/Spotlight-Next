package dev.bautista.spotlight.index

import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.SearchQuery
import dev.bautista.spotlight.search.normalize
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal data class CitationDocument(val uri:String,val title:String,val mime:String,val body:String,val metadata:String,val sha256:String,val fingerprint:String)
data class CitationRef internal constructor(val sourceId:String)
/** Literal snapshot, not an answer or enduring permission. Resolve again before displaying/copying/opening. */
data class DocumentCitation(val sourceId:String,val documentUri:String,val documentTitle:String,val quote:String,
 val start:Int,val end:Int,val sourceSha256:String,val page:Int?,val method:String,val uncertain:Boolean)

/** Explicit-query, bounded local retrieval. Not registered with the universal bar or any remote model.
 * Holds at most eight citations; no second index, embedding model, service or extraction cache. */
class DocumentCitationSession(private val index:FileIndex,private val config:()->Config):AutoCloseable {
 private val lifetime=Job()
 private val mutex=Mutex()
 private val hits=ConcurrentHashMap<String,Pair<DocumentCitation,String>>()
 override fun close(){lifetime.cancel();hits.clear()}
 private suspend fun <T> checked(block:suspend ()->T):T=coroutineScope {
  lifetime.ensureActive()
  val caller=currentCoroutineContext().job
  val hook=lifetime.invokeOnCompletion{caller.cancel()}
  try{withTimeout(10000){block().also{currentCoroutineContext().ensureActive();lifetime.ensureActive()}}}
  catch(e:CancellationException){close();throw e}
  finally{hook.dispose()}
 }
 suspend fun retrieve(query:String):List<CitationRef> =checked { mutex.withLock {
  hits.clear()
  require(query.length<=4000)
  val c=config();if(!c.files || !c.contentIndex || c.privateMode || query.isBlank())return@withLock emptyList()
  val plain=SearchQuery.parse(SearchQuery.natural(query)).plain
  val terms=normalize(plain).split(Regex("[^\\p{L}\\p{N}]+")).filter{it.length>=3 && it !in setOf("que","como","cual","donde","para","los","las","del","una")}
  if(terms.isEmpty())return@withLock emptyList()
  // Existing natural-query/lexical index order is authoritative. No model/network per keystroke.
  val candidates=index.search(query,c).take(8)
  val refs=mutableListOf<CitationRef>()
  for(result in candidates) {
   currentCoroutineContext().ensureActive();lifetime.ensureActive()
   val uri=(result.action as? Action.Launch)?.intent?.data?.toString() ?: continue
   val document=index.citationDocument(uri,config()) ?: continue
   val citation=literal(document,plain,terms) ?: continue
   val id=UUID.randomUUID().toString();val named=citation.copy(sourceId=id)
   hits[id]=named to document.fingerprint;refs+=CitationRef(id)
  }
  // Do not publish a batch that was revoked/deleted while other candidates were read.
  refs.filter{ref->
   val saved=hits[ref.sourceId] ?: return@filter false
   val fresh=index.citationDocument(saved.first.documentUri,config())
   (fresh?.fingerprint==saved.second).also{if(!it)hits.remove(ref.sourceId)}
  }
 }}
 suspend fun resolve(ref:CitationRef):DocumentCitation? {
  if(!lifetime.isActive)return null
  return checked { mutex.withLock {
   val saved=hits[ref.sourceId] ?: return@withLock null
   val current=index.citationDocument(saved.first.documentUri,config())
   if(current?.fingerprint!=saved.second){hits.remove(ref.sourceId);return@withLock null}
   saved.first
  }}
 }
 private fun literal(doc:CitationDocument,query:String,terms:List<String>):DocumentCitation? {
  if(doc.body.length>100000 || doc.metadata.length>30000)return null
  return runCatching {
   val root=JSONObject(doc.metadata);require(root.getInt("version")==1)
   val pages=root.getInt("pages")
   data class Part(val start:Int,val end:Int,val page:Int?,val method:String)
   val parts=if(pages>0) {
    require(doc.mime=="application/pdf")
    val fragments=root.getJSONArray("fragments");require(fragments.length() in 1..40)
    var previous=0
    (0 until fragments.length()).map { i ->
     val f=fragments.getJSONObject(i);val a=f.getInt("start");val b=f.getInt("end");val page=f.getInt("page");val method=f.getString("method")
     require(a>=previous && b>a && b<=doc.body.length && page in 1..pages && method in setOf("pdf_text","ocr_local"));previous=b
     Part(a,b,page,method)
    }
   } else {require(pages==0);listOf(Part(0,doc.body.length,null,"local_text"))}
   for(part in parts) {
    val text=doc.body.substring(part.start,part.end)
    if(terms.none{normalize(text).contains(it)})continue
    val around=DocumentExcerpt.around(text,query,600)
    val quote=if(text.length>600)around.removePrefix("…").removeSuffix("…") else around
    val local=text.indexOf(quote)
    if(quote.isBlank() || local<0 || text.indexOf(quote,local+1)>=0 || terms.none{normalize(quote).contains(it)})continue
    val start=part.start+local
    return DocumentCitation("",doc.uri,doc.title,quote,start,start+quote.length,doc.sha256,part.page,part.method,part.method=="ocr_local")
   }
   null
  }.getOrNull()
 }
}
