package dev.bautista.spotlight.ui

import android.app.Application
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class MasterC1MaterialTest {
 @Test fun freshDefaultsAndIndependentRoleAlpha(){
  val c=AppConfig()
  assertEquals(.84f,GlassTokens.opacity(c),0f)
  assertEquals(.92f,GlassTokens.actionOpacity(c),0f)
  assertEquals(.94f,GlassTokens.settingsOpacity(c),0f)
  assertEquals(.92f,GlassTokens.actionColor(c.copy(opacity=.70f),true).alpha,.005f)
  assertEquals(.94f,GlassTokens.settingsOpacity(c.copy(opacity=.70f)),0f)
 }
 @Test fun migrationPreservesLegacyAlphaAndUnrelatedPrivateFields()=runBlocking {
  val context=object:android.content.ContextWrapper(RuntimeEnvironment.getApplication()){
   override fun getApplicationContext():android.content.Context=this
   override fun getFilesDir()=java.io.File(super.getFilesDir(),"master-c1-${java.util.UUID.randomUUID()}").apply{mkdirs()}
  }
  val p=Preferences(context)
  assertEquals(.84f,p.config.first().opacity,0f)
  val old=p.config.first().copy(opacity=.97f,favorites=setOf("kept"),autoKeyboard="never")
  p.update(old)
  p.update(old.copy(privateMode=true,trees=setOf("content://kept")))
  p.updateAppearance(old.copy(actionOpacity=.77f,settingsOpacity=.88f,theme="dark"))
  val restored=Preferences(context).config.first()
  assertEquals(.97f,restored.opacity,0f);assertEquals(.77f,restored.actionOpacity,0f);assertEquals(.88f,restored.settingsOpacity,0f)
  assertTrue(restored.privateMode);assertEquals(setOf("content://kept"),restored.trees)
  assertEquals("never",restored.autoKeyboard);assertEquals(setOf("kept"),restored.favorites)
 }
 @Test fun bothBackupsRoundTripRolesAndLegacyBackupsKeepCurrentValues(){
  val c=AppConfig(opacity=.83f,actionOpacity=.91f,settingsOpacity=.96f)
  val encoded=listOf(ThemeTransfer.encode(c) to "visual",ConfigTransfer.encode(c) to "settings")
  for((text,key) in encoded){
   fun decode(s:String)=if(key=="visual")ThemeTransfer.decode(s,c) else ConfigTransfer.decode(s,c)
   val actual=decode(text)
   assertEquals(c.opacity,actual.opacity,0f);assertEquals(c.actionOpacity,actual.actionOpacity,0f);assertEquals(c.settingsOpacity,actual.settingsOpacity,0f)
   val legacy=JSONObject(text);legacy.getJSONObject(key).remove("actionOpacity");legacy.getJSONObject(key).remove("settingsOpacity")
   val kept=decode(legacy.toString());assertEquals(c.actionOpacity,kept.actionOpacity,0f);assertEquals(c.settingsOpacity,kept.settingsOpacity,0f)
   for(bad in listOf(-.1,1.1,Double.POSITIVE_INFINITY)){
    val j=JSONObject(text)
    try{j.getJSONObject(key).put("actionOpacity",bad);decode(j.toString());fail("Invalid opacity accepted: $bad")}catch(expected:IllegalArgumentException){}catch(expected:org.json.JSONException){}
   }
  }
 }
}
