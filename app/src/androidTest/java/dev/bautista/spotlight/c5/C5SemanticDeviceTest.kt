package dev.bautista.spotlight.c5

import android.os.SystemClock
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.DeviceResources
import dev.bautista.spotlight.index.*
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.semantic.SemanticPolicy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class C5SemanticDeviceTest {
    @Test fun optionalAuthenticModelComparisonOnTheUnchangedC53Corpus()=runBlocking {
        val lab=C5PhysicalLab()
        lab.runCase("semantic") {
            lab.gate("semantic",lab.args.getString("c5_semantic")=="true","Authentic semantic evaluation not explicitly selected")
            val prefs=(lab.target.applicationContext as SpotlightApp).preferences
            val initial=withTimeout(10000){prefs.config.first()}
            fun current()=(prefs.current ?: initial).let{it.copy(privateMode=it.privateMode || prefs.isPrivate)}
            lab.gate("semantic",current().let{it.files && it.contentIndex && it.semanticSearch && !it.privateMode},"Existing effective preferences do not authorize semantic search")
            val resources=DeviceResources.read(lab.target)
            lab.gate("semantic",SemanticPolicy.deferred(resources)==null,"Real resource policy: "+SemanticPolicy.deferred(resources))
            val index=lab.index{current()}
            val readyStart=SystemClock.elapsedRealtimeNanos()
            lab.gate("semantic",index.semanticModel.ready(),"No verified model in THIS target package; nothing downloaded")
            val readyNanos=SystemClock.elapsedRealtimeNanos()-readyStart
            try {
            val corpusBytes=lab.asset("corpus.json");val corpus=JSONObject(String(corpusBytes,Charsets.UTF_8))
            val docs=corpus.getJSONArray("documents");val ids=mutableMapOf<String,String>()
            for(i in 0 until docs.length()) {
                val d=docs.getJSONObject(i);val name=d.getString("filename")
                val bytes=when {
                    d.has("ocr_fixture_text")->lab.asset("ocr-raster-fixture.pdf")
                    d.has("fixture")->lab.asset(d.getString("fixture"))
                    else->d.getString("text").toByteArray(Charsets.UTF_8)
                }
                if(d.has("sha256"))assertEquals(d.getString("sha256"),lab.sha(bytes))
                val file=lab.file(name,bytes);val mime=if(name.endsWith(".pdf"))"application/pdf" else "text/plain"
                val controlled=if(d.has("ocr_fixture_text"))d.getString("ocr_fixture_text").let{text->
                    ExtractedDocument(text,listOf(TextFragment(1,"ocr_local",0,text.length)),emptyList(),1,1,1,lab.sha(bytes))
                }else null
                lab.seed(index,file,mime,current(),controlled)
                ids[android.net.Uri.fromFile(file).toString()]=d.getString("id")
            }
            val pssBefore=lab.pssKiB();val buildStart=SystemClock.elapsedRealtimeNanos()
            // Explicit preparation of ONLY isolated fixture rows, with production resource/preference gates.
            index.buildSemantic(current())
            val buildNanos=SystemClock.elapsedRealtimeNanos()-buildStart
            lab.report("semantic-build",JSONObject().put("message",index.semanticStatus.value.message)
                .put("fragments",index.semanticStatus.value.fragments).put("partial",index.semanticStatus.value.partial))
            assertTrue("Model ready but preparation failed: "+index.semanticStatus.value.message,index.semanticStatus.value.fragments>0)
            val cases=JSONArray();val questions=corpus.getJSONArray("queries")
            for(i in 0 until questions.length()) {
                lab.gate("semantic",current().let{it.files && it.contentIndex && it.semanticSearch && !it.privateMode} && SemanticPolicy.deferred(DeviceResources.read(lab.target))==null,"Preferences/resources changed during evaluation")
                val q=questions.getJSONObject(i);val text=q.getString("query")
                val expected=q.getJSONArray("relevant").let{a->(0 until a.length()).map{a.getString(it)}.toSet()}
                val start=SystemClock.elapsedRealtimeNanos();val lexical=index.search(text,current())
                val lexicalNanos=SystemClock.elapsedRealtimeNanos()-start
                val semStart=SystemClock.elapsedRealtimeNanos();val semantic=index.semanticSearch(text,current())
                val semanticNanos=SystemClock.elapsedRealtimeNanos()-semStart
                fun resultRows(results:List<dev.bautista.spotlight.search.Result>)=JSONArray(results.map{r->
                    JSONObject().put("id",ids.getValue((r.action as Action.Launch).intent.data.toString()))
                        .put("api_score",r.score).put("semantic_match",r.semanticMatch)
                })
                fun metrics(results:List<dev.bautista.spotlight.search.Result>):JSONObject {
                    val order=results.map{ids.getValue((it.action as Action.Launch).intent.data.toString())}
                    val actual=order.toSet();val rank=order.indexOfFirst{it in expected}
                    return JSONObject().put("tp",(actual intersect expected).size).put("fp",(actual-expected).size)
                        .put("fn",(expected-actual).size).put("reciprocal_rank",if(rank<0)0.0 else 1.0/(rank+1))
                }
                val combined=(lexical+semantic).distinctBy{it.id} // Lab comparison, NOT a production citation bridge.
                cases.put(JSONObject().put("id",q.getString("id")).put("query",text).put("expected",q.getJSONArray("relevant"))
                    .put("lexical",resultRows(lexical)).put("semantic",resultRows(semantic)).put("lexical_then_optional",resultRows(combined))
                    .put("lexical_metrics",metrics(lexical)).put("semantic_metrics",metrics(semantic)).put("combined_metrics",metrics(combined))
                    .put("lexical_ns",lexicalNanos).put("semantic_ns",semanticNanos))
                // Flush after each actual query, so a later failure never fabricates remaining measurements.
                lab.report("semantic-cases",JSONObject().put("status","IN_PROGRESS").put("completed_queries",i+1).put("cases",cases))
            }
            JSONObject().put("model",SemanticPolicy.MODEL).put("corpus_sha256",lab.sha(corpusBytes))
                .put("ready_verify_ns",readyNanos).put("build_ns",buildNanos).put("fragments",index.semanticStatus.value.fragments)
                .put("partial",index.semanticStatus.value.partial).put("pss_kib_before",pssBefore).put("pss_kib_after",lab.pssKiB())
                .put("pss_is_whole_process_not_model_peak",true).put("cases",cases)
                .put("ocr_transcription_controlled_for_exact_c53_pairing",true)
                .put("semantic_results_are_not_validated_citations",true).put("no_thresholds_changed",true)
                .put("empty_api_results_cannot_distinguish_no_match_from_internal_inference_failure",true)
            } finally {index.cancelSemanticWork()}
        }
    }
}
