package dev.bautista.spotlight.ui

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class SportsAgendaMergeTest {
    private fun entry(time:Long,score:String,stale:Boolean)=SportsAgendaEntry(JSONObject().put("idEvent","fixture-event").put("intHomeScore",score),time,stale,stale)
    @Test fun duplicateEncounterKeepsNewestObservationInsteadOfFirstFavoriteResponse() {
        val old=entry(100,"0",true);val fresh=entry(200,"1",false)
        assertEquals("1",SportsAgendaOrder.merge(listOf(old,fresh)).single().json.getString("intHomeScore"))
        assertEquals(200L,SportsAgendaOrder.merge(listOf(fresh,old)).single().fetchedAt)
    }
    @Test fun equalTimestampPrefersSuccessfulSourceOverStaleFallback() {
        val old=entry(100,"0",true);val fresh=entry(100,"1",false)
        assertFalse(SportsAgendaOrder.merge(listOf(old,fresh)).single().stale)
        assertFalse(SportsAgendaOrder.merge(listOf(fresh,old)).single().stale)
    }
}
