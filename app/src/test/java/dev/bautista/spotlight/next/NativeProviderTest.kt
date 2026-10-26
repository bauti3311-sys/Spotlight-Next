package dev.bautista.spotlight.next
import android.app.Application
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
class NativeProviderTest {
 @Test fun weatherUsesCacheAndPreservesOfflineTimestamp()=runBlocking {
  val app=RuntimeEnvironment.getApplication();app.getSharedPreferences("native_provider_cache_v1",0).edit().clear().commit();var now=1_000_000L;var calls=0;var offline=false
  val repo=NativeProviders(app,{url->calls++;check(url.host=="api.open-meteo.com");check(url.queryParameter("latitude")=="-38.0023");if(offline)error("offline");JSONObject().put("current",JSONObject().put("temperature_2m",15))},{now})
  val first=repo.weather();assertEquals(1,calls);assertFalse(first.stale);assertFalse(first.fromCache);assertEquals("api.open-meteo.com",first.source);val hit=repo.weather(refresh=true);assertTrue(hit.fromCache);assertEquals(first.fetchedAt,hit.fetchedAt);assertEquals(1,calls)
  now+=16*60_000;offline=true;val cached=repo.weather();assertEquals(2,calls);assertTrue(cached.stale);assertTrue(cached.fromCache);assertEquals("api.open-meteo.com",cached.source);assertEquals(first.fetchedAt,cached.fetchedAt);assertEquals(15,cached.json.getJSONObject("current").getInt("temperature_2m"))
 }
 @Test fun unknownCityDoesNotSilentlyUseDefault()=runBlocking {
  val app=RuntimeEnvironment.getApplication();app.getSharedPreferences("native_provider_cache_v1",0).edit().clear().commit()
  val repo=NativeProviders(app,{JSONObject()});try{repo.weather("Ciudad inexistente");fail()}catch(e:IllegalStateException){assertTrue(e.message!!.contains("localidad"))}
 }
 @Test fun sportsOnlyUsesDocumentedPublicEndpoint()=runBlocking {
  val app=RuntimeEnvironment.getApplication();app.getSharedPreferences("native_provider_cache_v1",0).edit().clear().commit()
  val repo=NativeProviders(app,{url->assertEquals("www.thesportsdb.com",url.host);assertEquals("/api/v1/json/123/eventsday.php",url.encodedPath);assertEquals("Basketball",url.queryParameter("s"));JSONObject().put("events",org.json.JSONArray())})
  assertEquals(0,repo.sports("Basketball").json.getJSONArray("events").length())
 }
}
