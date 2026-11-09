package dev.bautista.spotlight.experiments.square

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Original protocol study, test source ONLY. No Media3 dependency, Binder or actual Square.
 * This fixture cannot establish connection compatibility or real audio on a device.
 */
class SquareProtocolFixtureTest {
    private data class Item(val id: String)
    private data class Observation(val id: String?, val playing: Boolean)
    private data class Result(val requested: Boolean, val observedSelection: Boolean)
    private interface Browser {
        val searchAllowed: Boolean
        val playbackAllowed: Boolean
        suspend fun searchAndGetResults(query: String): List<Item>
        fun setMediaItem(item: Item)
        fun prepare()
        fun play()
        fun observe(): Observation
        fun release()
    }
    private class Fixture(
        override val searchAllowed: Boolean = true,
        override val playbackAllowed: Boolean = true,
        val blockSearch: Boolean = false,
        val failSearch: Boolean = false,
    ) : Browser {
        val calls = mutableListOf<String>()
        val item = Item("opaque-fixture-result:keep-the-returned-id")
        var state = Observation(null, false)
        override suspend fun searchAndGetResults(query: String): List<Item> {
            calls += "search:$query"
            if (blockSearch) awaitCancellation()
            if (failSearch) error("Fixture search denied")
            calls += "getSearchResult:0:20"
            return listOf(item)
        }
        override fun setMediaItem(item: Item) { assertSame(this.item, item); calls += "setMediaItem" }
        override fun prepare() { calls += "prepare" }
        override fun play() { calls += "play" } // Does not manufacture an observed player update.
        override fun observe(): Observation { calls += "observe"; return state }
        override fun release() { calls += "release" }
    }
    private suspend fun study(browser: Browser, pickAndConfirm: (List<Item>) -> Item?): Result {
        try {
            check(browser.searchAllowed)
            val items = browser.searchAndGetResults("fixture query")
            val selected = pickAndConfirm(items) ?: return Result(false, false)
            check(items.any { it === selected })
            check(browser.playbackAllowed)
            currentCoroutineContext().ensureActive()
            browser.setMediaItem(selected); browser.prepare(); browser.play()
            val observed = browser.observe()
            return Result(true, observed.playing && observed.id == selected.id)
        } finally { browser.release() }
    }
    @Test fun explicitSelectionUsesTheReturnedItemAndDoesNotInventPlayback() = runBlocking {
        val f=Fixture()
        val result=study(f) { it.single() }
        assertTrue(result.requested); assertFalse(result.observedSelection)
        assertEquals(listOf("search:fixture query","getSearchResult:0:20","setMediaItem","prepare","play","observe","release"),f.calls)
    }
    @Test fun cancellationBeforeConfirmationNeverRequestsPlayback() = runBlocking {
        val f=Fixture()
        assertFalse(study(f) { null }.requested)
        assertFalse(f.calls.contains("play")); assertEquals("release",f.calls.last())
    }
    @Test fun revokedPlaybackCommandNeverFallsBackToAnActivityOrRetries() = runBlocking {
        val f=Fixture(playbackAllowed=false)
        assertTrue(runCatching { study(f) { it.single() } }.isFailure)
        assertFalse(f.calls.contains("setMediaItem")); assertEquals(1,f.calls.count { it=="release" })
    }
    @Test fun deniedOrFailedSearchReleasesTheHandle() = runBlocking {
        for(f in listOf(Fixture(searchAllowed=false),Fixture(failSearch=true))) {
            assertTrue(runCatching { study(f) { it.single() } }.isFailure)
            assertFalse(f.calls.contains("play")); assertEquals(1,f.calls.count { it=="release" })
        }
    }
    @Test fun coroutineCancellationReleasesWithoutAPlayRequest() = runBlocking {
        val f=Fixture(blockSearch=true)
        val operation=launch(start=CoroutineStart.UNDISPATCHED) { study(f) { it.single() } }
        operation.cancelAndJoin()
        assertFalse(f.calls.contains("play")); assertEquals(1,f.calls.count { it=="release" })
    }
    @Test fun onlyAnIndependentMatchingPlayingObservationConfirmsSelection() = runBlocking {
        for(match in listOf(false,true)) {
            val f=Fixture(); f.state=Observation(if(match)f.item.id else "another-item",true)
            assertEquals(match,study(f) { it.single() }.observedSelection)
            assertEquals(1,f.calls.count { it=="play" })
        }
    }
}
