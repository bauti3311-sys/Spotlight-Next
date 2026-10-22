package dev.bautista.spotlight.ui
import dev.bautista.spotlight.search.*
import org.junit.Test
import org.junit.Assert.*
class AiRecoveryPrivacyTest {
 @Test fun changingDestinationRequiresReviewOfAllPriorContext() {
  val turns=listOf("user" to "Documento privado","assistant" to "Respuesta")
  val request=AiRequestPolicy.question("Continuá",true)
  assertFalse(AiRequestPolicy.requiresReview(AiRequestPolicy.revalidateContinuation(request,"same","same",turns)))
  val changed=AiRequestPolicy.revalidateContinuation(request,"old","new",turns)
  assertTrue(AiRequestPolicy.requiresReview(changed));assertEquals(turns,changed.priorMessages)
  val confirmed=AiRequestPolicy.authorize(changed)
  assertFalse(AiRequestPolicy.requiresReview(AiRequestPolicy.revalidateContinuation(confirmed,"old","new",turns)))
 }
 @Test fun completedAndInterruptedContextSnapshotsRemainSeparateAfterSerialization() {
  val cache=InspectorAiSessions()
  val one=taskFingerprint("provider A and context A")
  val two=taskFingerprint("provider B and context B")
  cache.put(one,InspectorAiSnapshot("Uno",listOf("user" to "A","assistant" to "Uno"),"",true))
  cache.put(two,InspectorAiSnapshot("Dos",listOf("user" to "B","assistant" to "Dos"),"",false))
  val restored=InspectorAiSessions.restore(cache.saved())
  assertEquals("Uno",restored.get(one)!!.text);assertTrue(restored.get(one)!!.interrupted)
  assertEquals("Dos",restored.get(two)!!.text);assertFalse(restored.get(two)!!.interrupted)
  assertEquals(64,one.length);assertFalse(cache.saved().contains("provider A and context A"))
 }
}
