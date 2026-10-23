package dev.bautista.spotlight.ui

import android.media.AudioManager
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.LocalPlan
import dev.bautista.spotlight.search.PlanStep
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class RelativePlanChainTest {
 @Test fun relativeVolumeIsReviewedAgainstThePrecedingAbsoluteStep()=runBlocking {
  val app=RuntimeEnvironment.getApplication() as SpotlightApp
  val audio=app.getSystemService(AudioManager::class.java)
  audio.setStreamVolume(AudioManager.STREAM_MUSIC,0,0)
  val rows=resolvePlan(app,AppConfig(),LocalPlan(listOf(PlanStep.Volume(40),PlanStep.RelativeVolume(10))))
  assertEquals("40",rows[0].call?.arguments?.get("percent"))
  assertEquals("50",rows[1].call?.arguments?.get("percent"))
  assertEquals("Preview must not execute changes",0,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
 }
}
