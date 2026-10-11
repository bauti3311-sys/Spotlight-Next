package dev.bautista.spotlight.control

import android.app.Application
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class IntegrationRegistryTest {
    @Test fun appHubUsesCanonicalLauncherIdentityForFavoritesAndRecents() = runBlocking {
        val context=RuntimeEnvironment.getApplication()
        val launch=context.packageManager.getLaunchIntentForPackage(context.packageName)!!
        val component=launch.component!!
        val row=IntegrationRegistry(context).menu("spotlight",launch,context.packageName).first { it.title=="Abrir Spotlight" }
        assertEquals("${component.packageName}/${component.className}",row.id)
        assertEquals("Aplicaciones",row.category)
        assertTrue(row.action is Action.Launch)
    }
}
