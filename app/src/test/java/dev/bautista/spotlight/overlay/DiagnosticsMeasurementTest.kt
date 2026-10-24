package dev.bautista.spotlight.overlay

import android.app.Application
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class DiagnosticsMeasurementTest {
    @Test fun zeroClockAndRepeatedCallbacksProduceOneMeasuredSample() {
        var now=0L;val d=Diagnostics(RuntimeEnvironment.getApplication()){now}
        d.beginOpening();d.markVisible();now=12;d.markInteractive();d.markVisible();d.markInteractive()
        assertEquals(listOf(OpeningSample(0,12,true)),d.openings.value)
    }
    @Test fun firstOverlayInvocationIsNeverClaimedAsColdProcessStartup() {
        var now=10000L;val d=Diagnostics(RuntimeEnvironment.getApplication()){now}
        d.beginOpening();now+=10;d.markVisible();now+=5;d.markInteractive()
        val report=JSONObject(d.report());assertTrue(report.isNull("coldFirstDrawMs"));assertEquals(10,report.getLong("firstOverlayDrawMs"))
        assertTrue(report.isNull("warmFirstDrawP95Ms"))
        d.beginOpening();now+=20;d.markVisible();now+=5;d.markInteractive()
        assertEquals(20,JSONObject(d.report()).getLong("warmFirstDrawP95Ms"))
    }
    @Test fun backwardsClockAndIncompleteOpeningsDoNotInventDurations() {
        var now=100L;val d=Diagnostics(RuntimeEnvironment.getApplication()){now}
        d.beginOpening();now=90;d.markVisible();d.markInteractive();assertTrue(d.openings.value.isEmpty())
        now=101;d.markVisible();d.markInteractive();assertEquals(1,d.openings.value.single().visibleMs)
    }
    @Test fun clearAbandonsInFlightMeasurementWithoutResettingProcessIdentity() {
        var now=100L;val d=Diagnostics(RuntimeEnvironment.getApplication()){now}
        d.beginOpening();d.clear();now=150;d.markVisible();d.markInteractive();assertTrue(d.openings.value.isEmpty())
        d.beginOpening();now=151;d.markVisible();d.markInteractive();assertFalse(d.openings.value.single().firstInProcess)
    }
    @Test fun reportExcludesNegativeSamplesAndLabelsHeapWithoutClaimingRamBenchmark() {
        val d=Diagnostics(RuntimeEnvironment.getApplication());d.search(-1);d.search(0);d.search(50)
        val report=JSONObject(d.report());assertEquals(2,report.getInt("searchSamples"));assertEquals(50,report.getLong("searchP95Ms"))
        assertTrue(report.getLong("javaHeapUsedBytes")>=0);assertTrue(report.getString("memoryMeasurement").contains("not total process RAM"))
    }
}
