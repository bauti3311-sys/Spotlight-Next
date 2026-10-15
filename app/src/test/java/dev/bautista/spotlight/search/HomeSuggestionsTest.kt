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
    @Test fun freshInstallHasRealLaunchableAppsWithoutLearningOrUsageAccess() {
        val launcher=android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        listOf("Chrome","Spotify","WhatsApp").forEachIndexed { i,label ->
            val info=android.content.pm.ActivityInfo().apply {packageName="fixture.home$i";name="$packageName.Main";nonLocalizedLabel=label;applicationInfo=android.content.pm.ApplicationInfo().apply{packageName="fixture.home$i"}}
            Shadows.shadowOf(context.packageManager).addResolveInfoForIntent(launcher,android.content.pm.ResolveInfo().apply{activityInfo=info})
        }
        val rows=HomeSuggestions.results(context,AppConfig(learning=false,files=false),false,false)
        assertEquals(listOf("Chrome","Spotify","WhatsApp"),rows.filter{it.action is Action.Launch}.map{it.title})
        assertTrue(rows.none{it.subtitle.contains("frecuente") || it.subtitle.contains("reciente")})
    }

}
