package dev.bautista.spotlight.search
import android.content.Intent
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class MasterC1ResultsTest {
 @Test fun homeThreeButQuerySixAndExpansionPreservesAllIdentities(){
  val rows=(1..12).map{Result("app$it","Spotify $it","Abrir aplicación","Aplicaciones",1000,Action.Launch(Intent("open.$it")))}
  val view=CompactResults.select("spot",rows,AppConfig())
  assertEquals(6,view.initial.size)
  assertEquals(12,view.visible(true).size)
  assertEquals(6,view.visible(false,"app9").size)
  assertTrue(view.visible(false,"app9").any{it.id=="app9"})
  assertTrue(CompactResults.select("",rows,AppConfig()).initial.size<=3)
 }
 @Test fun sparseMatchesAreNeverFilledWithGenericFallback(){
  val app=Result("spotify","Spotify","","Aplicaciones",1,Action.Launch(Intent("spotify")))
  val noise=Result("web","Buscar en web","","Web",99999,Action.Launch(Intent("web")))
  assertEquals(listOf("spotify"),CompactResults.select("spot",listOf(noise,app),AppConfig()).initial.map{it.id})
 }
}
