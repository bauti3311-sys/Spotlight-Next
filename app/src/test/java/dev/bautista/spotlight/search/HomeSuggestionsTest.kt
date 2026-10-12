package dev.bautista.spotlight.search

import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=android.app.Application::class)
class HomeSuggestionsTest {
    private val context get()=RuntimeEnvironment.getApplication()
    @Test fun noHistoryOrUnavailableProvidersLeakIntoHome(){val rows=HomeSuggestions.results(context,AppConfig(files=false,actionRecent=listOf("Privado"),recent=listOf("old/app")),false,false);assertTrue(rows.size in 0..4);assertTrue(rows.none{it.title=="Privado" || it.id=="suggest:ai" || it.id=="suggest:files"});assertEquals(rows.size,rows.distinctBy{it.id}.size)}
    @Test fun hideAndPinAreAppliedToAvailableCommands(){val rows=HomeSuggestions.results(context,AppConfig(files=false,hiddenSuggestions=setOf("suggest:note"),pinnedSuggestions=setOf("suggest:commands")),false,false);assertTrue(rows.none{it.id=="suggest:commands"});assertTrue(rows.none{it.id=="suggest:note"})}
    @Test fun disabledSuggestionsReturnNothing(){assertTrue(HomeSuggestions.results(context,AppConfig(suggestions=false),true,true).isEmpty())}
}
