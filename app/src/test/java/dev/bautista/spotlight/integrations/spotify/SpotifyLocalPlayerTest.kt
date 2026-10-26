package dev.bautista.spotlight.integrations.spotify
import kotlinx.coroutines.*
import org.junit.Test
import org.junit.Assert.*

class SpotifyLocalPlayerTest {
 private val item=SpotifySearchItem(SpotifyItemType.TRACK,"a".repeat(22),"Fixture","Artist")
 private class Link:SpotifyRemoteLink {
  val calls=mutableListOf<String>()
  var allowed=true
  var fail=false
  override suspend fun canPlayOnDemand():Boolean {calls+="capability";return allowed}
  override suspend fun play(uri:String) {calls+=uri;if(fail)error("fixture failure")}
  override suspend fun observe()=SpotifyPlaybackObservation(true,null,null,0)
  override fun close(){calls+="close"}
 }
 @Test fun authorizationDoesNotPlayAndSelectedPlayDoesNotShowAuthorization()=runBlocking {
  val link=Link();val authorization=mutableListOf<Boolean>()
  val player=SpotifyLocalPlayer{_,auth->authorization+=auth;link}
  player.authorize("a".repeat(32))
  assertEquals(listOf("close"),link.calls)
  link.calls.clear()
  assertTrue(player.play("a".repeat(32),item).contains("Solicitud aceptada"))
  assertEquals(listOf(true,false),authorization)
  assertEquals(listOf("capability",item.uri,"close"),link.calls)
 }
 @Test fun unsupportedOnDemandPlaybackDoesNotSendPlay()=runBlocking {
  val link=Link().apply{allowed=false}
  val player=SpotifyLocalPlayer{_,_->link}
  assertTrue(runCatching{player.play("a".repeat(32),item)}.isFailure)
  assertEquals(listOf("capability","close"),link.calls)
 }
 @Test fun failedPlaybackIsNotRetriedOrFollowedByARecoveryCommand()=runBlocking {
  val link=Link().apply{fail=true}
  val player=SpotifyLocalPlayer{_,_->link}
  assertTrue(runCatching{player.play("a".repeat(32),item)}.isFailure)
  assertEquals(listOf("capability",item.uri,"close"),link.calls)
 }
 @Test fun cancellationDuringConnectionNeverSendsPlayback()=runBlocking {
  var opened=0
  val player=SpotifyLocalPlayer{_,_->opened++;awaitCancellation()}
  val task=launch(start=CoroutineStart.UNDISPATCHED){player.play("a".repeat(32),item)}
  task.cancelAndJoin()
  assertEquals(1,opened);assertTrue(task.isCancelled)
 }
}
