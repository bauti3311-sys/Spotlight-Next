package dev.bautista.spotlight.search

import android.content.Intent
import android.provider.DocumentsContract
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class CompactResultsTest {
    private fun app(id:String,title:String)=Result(id,title,"Abrir aplicación","Aplicaciones",1000,Action.Launch(Intent("open.$id")))
    private fun select(q:String,rows:List<Result>)=CompactResults.select(q,ResultRanker.rank(q,rows,AppConfig()),AppConfig())
    @Test fun exactAppBeatsHugeSecondaryScoresAndNoFillerIsAdded(){
        val exact=app("exact","webOS Dev Manager")
        val noise=listOf(app("wa","WhatsApp"),Result("wifi","Ajustes Wi-Fi","","Comandos",99999,Action.Launch(Intent("wifi"))))
        val view=select("webOS Dev Manager",noise+exact)
        assertEquals(listOf("exact"),view.initial.map{it.id})
        assertEquals(3,view.all.size)
    }
    @Test fun whatsappKeepsUsefulComposeButNotCacheDictionaryOrGenericWeb(){
        val rows=listOf(app("wa","WhatsApp"),Result("message","Enviar mensaje","Elegir contacto","Acciones",600,Action.ComposeMessage("whatsapp")),
            Result("cache","WhatsApp Cache","","Archivos",9999,Action.Launch(Intent("cache")),mime=DocumentsContract.Document.MIME_TYPE_DIR),
            Result("web","Buscar WhatsApp","","Web",9999,Action.Launch(Intent("web"))),
            Result("concept-definition","WhatsApp definición","","Información",9999,Action.Lookup("dictionary","WhatsApp")))
        val view=select("WhatsApp",rows)
        assertEquals(listOf("wa","message"),view.initial.map{it.id})
        assertEquals(5,view.visible(true).size)
    }
    @Test fun recognizedBrightnessMusicAndQuestionActionsStayEligible(){
        val queries=listOf("brillo 40%","música de Cerati","explicame el reflejo vestibuloocular","mandar WhatsApp a Juan")
        val actions=listOf<Action>(Action.Device("brightness:40"),Action.ComposeSearch("spotify","com.spotify.music","Cerati"),AiRequestPolicy.question(queries[2]),Action.ComposeMessage("whatsapp","Juan"))
        queries.zip(actions).forEach{(q,a)->
            val result=Result("root:test","Acción reconocida","Revisar antes de ejecutar","Acciones",3400,a)
            assertEquals(listOf(result.id),select(q,listOf(result)).initial.map{it.id})
        }
    }
    @Test fun contentAndSemanticDocumentHitsDoNotRequireTitleOverlap(){
        val doc=Result("doc","Apuntes.pdf","Anatomía vestibular · página 2 OCR","Archivos",700,Action.Launch(Intent("doc")),mime="application/pdf")
        val sem=doc.copy(id="semantic",action=Action.Launch(Intent("semantic")),semanticMatch=true)
        assertEquals(2,select("PDF anatomía",listOf(doc,sem)).initial.size)
    }
    @Test fun queryShowsSixAndExpansionRetainsEveryDistinctAction(){
        val rows=(1..12).map{app("app$it","Chrome $it")}
        val view=select("Chrome",rows)
        assertEquals(6,view.initial.size);assertEquals(12,view.visible(true).size)
        val collapsed=view.visible(false,"app9")
        assertEquals(6,collapsed.size);assertTrue(collapsed.any{it.id=="app9"})
        assertEquals(12,view.visible(true).size)
        assertEquals(3,CompactResults.select("",rows,AppConfig()).initial.size)
    }
    @Test fun duplicateActionsAndNoiseOnlyQueriesDoNotFillThreeSlots(){
        val a=app("app","WhatsApp")
        assertEquals(1,select("WhatsApp",listOf(a,a.copy(id="duplicate"))).initial.size)
        assertTrue(select("unrelated",listOf(app("app","WhatsApp"))).initial.isEmpty())
        assertEquals("",CompactResults.subtitle(a))
        assertEquals("Favorita",CompactResults.subtitle(a.copy(subtitle="Favorita")))
    }
    @Test fun explicitDictionaryAndTextCommandsRemainAvailableInitially(){
        val lookup=Result("lookup:dictionary","Consultar diccionario","definir ataxia","Información",1750,Action.Lookup("dictionary","ataxia"))
        assertEquals(listOf(lookup.id),select("definir ataxia",listOf(lookup)).initial.map{it.id})
        val chat=Result("conversation:1","Mi glosario","Guardada","IA",500,Action.SystemPanel("conversation","1"))
        assertEquals(listOf(chat.id),select("conversaciones",listOf(chat)).initial.map{it.id})
        val pin=Result("pin:volumen al 40%","volumen al 40%","Consulta fijada","Consultas",850,Action.RestoreQuery("volumen al 40%"))
        assertEquals(listOf(pin.id),select("fijados",listOf(pin)).initial.map{it.id})
    }
    @Test fun explicitAiNotificationsAndAndroidCommandsSurviveDifferentLabels(){
        val cases=listOf(
            "ia hola" to Result("ai","Preguntar a la IA","hola","IA",2400,AiRequestPolicy.question("hola")),
            "mensajes" to Result("reply:1","Juan","Notificación actual","Mensajes",2200,Action.ReadNotification("1")),
            "email juan@example.test" to Result("action:email","Redactar correo","Revisar","Comandos",1600,Action.Launch(Intent("compose"))))
        cases.forEach{(q,row)->assertEquals(listOf(row.id),select(q,listOf(row)).initial.map{it.id})}
    }
    @Test fun explicitUrlProviderAndFolderRequestsAreNotTreatedAsGenericFallbacks(){
        val url=Result("url","https://example.test","Abrir dirección","Web",1300,Action.Launch(Intent("url")))
        assertEquals(listOf("url"),select("https://example.test",listOf(url)).initial.map{it.id})
        val web=url.copy(id="search:natural:google",title="Buscar en Google")
        assertEquals(listOf(web.id),select("google anatomía",listOf(web)).initial.map{it.id})
        val folder=url.copy(id="folder",title="Anatomía",category="Archivos",mime=DocumentsContract.Document.MIME_TYPE_DIR)
        assertTrue(select("anatomía",listOf(folder)).initial.isEmpty())
        assertEquals(listOf(folder.id),select("kind:folder anatomía",listOf(folder)).initial.map{it.id})
    }
    @Test fun explicitAppAndContactScopesUseTheirParsedSearchTerms(){
        val rows=(1..6).map{app("app$it","Chrome $it")}+app("exact","Chrome")
        val view=select("/apps Chrome",rows)
        assertEquals("exact",view.initial.first().id);assertEquals(6,view.initial.size)
        assertEquals(7,view.all.size)
        val contact=Result("contact:1","Juan","Contacto autorizado","Contactos",700,Action.ComposeMessage("sms","Juan"))
        assertEquals(listOf(contact.id),select("/contacts Juan",listOf(contact)).initial.map{it.id})
    }
    @Test fun exactApplicationNeverAutocompletesIntoLongerSibling(){
        val rows=listOf(app("exact","Chrome"),app("beta","Chrome Beta"))
        assertNull(dev.bautista.spotlight.control.CommandCenter.complete("Chrome",rows))
        assertNull(dev.bautista.spotlight.control.CommandCenter.complete("Chro",rows))
        assertEquals("Chrome",dev.bautista.spotlight.control.CommandCenter.complete("Chro",rows.take(1)))
    }
    @Test fun navigationKeepsExistingOrderWhenLateProvidersArrive(){
        val rows=listOf(app("b","Chrome Beta"),app("a","Chrome"))
        val view=CompactResults.select("Chrome",rows,AppConfig(),preserveOrder=true)
        assertEquals(listOf("b","a"),view.initial.map{it.id})
    }
}
