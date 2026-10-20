package dev.bautista.spotlight.ui

import android.content.ComponentName
import android.content.Intent
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A home suggestion backed by a real launcher intent is an app, not a new mode. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class PreviewPresentationContractTest {
    private fun launch()=Action.Launch(
        Intent(Intent.ACTION_MAIN).setComponent(ComponentName("com.spotify.music","com.spotify.music.Main"))
    )

    @Test fun favoriteAppInHomeSuggestionsHasRealInspector() {
        val spotify=Result("suggest:app:com.spotify.music","Spotify","Favorita",
            "Sugerencias",500,launch())
        assertTrue(PreviewPresentation.isApplication(spotify))
        assertFalse(PreviewPresentation.needsInspector(spotify))
    }

    @Test fun normalAppSearchResultKeepsInspectorEligibility() {
        val spotify=Result("com.spotify.music/com.spotify.music.Main","Spotify","App",
            "Aplicaciones",900,launch())
        assertTrue(PreviewPresentation.isApplication(spotify))
    }

    @Test fun arbitrarySuggestionsCannotMasqueradeAsApps() {
        val note=Result("suggest:note:example","Nota","Abierta","Sugerencias",300,Action.Copy("texto"))
        val forged=Result("suggest:app:com.spotify.music","Falso","No es app",
            "Sugerencias",300,Action.Copy("texto"))
        assertFalse(PreviewPresentation.isApplication(note))
        assertFalse(PreviewPresentation.isApplication(forged))
        assertFalse(PreviewPresentation.isApplication(null))
    }
}
