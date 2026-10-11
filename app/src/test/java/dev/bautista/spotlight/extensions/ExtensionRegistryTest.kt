package dev.bautista.spotlight.extensions

import android.app.Application
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class ExtensionRegistryTest {
    @Test fun commandsAndExtensionsHaveUniqueIds(){val modules=ExtensionRegistry.extensions;assertEquals(modules.size,modules.map{it.id}.toSet().size);val commands=modules.flatMap{it.commands};assertEquals(commands.size,commands.map{it.id}.toSet().size);assertTrue(commands.all{ExtensionRegistry.resolve(it.id)==it});assertNull(ExtensionRegistry.resolve("shell.execute"));assertTrue(modules.all{it.explanation.isNotBlank()})}
    @Test fun missingDependenciesAndDisabledProvidersAreExplicit()=runBlocking{val items=ExtensionRegistry.snapshot(RuntimeEnvironment.getApplication(),AppConfig(files=false,contacts=false,online=false));assertEquals(ExtensionAvailability.DEPENDENCY,items.first{it.definition.id=="whatsapp"}.availability);assertEquals(ExtensionAvailability.CONFIGURATION,items.first{it.definition.id=="contacts"}.availability);assertEquals(ExtensionAvailability.CONFIGURATION,items.first{it.definition.id=="weather"}.availability);assertEquals(ExtensionAvailability.AVAILABLE,items.first{it.definition.id=="notes"}.availability)}
    @Test fun openingRegistryKeepsARealTypedAction(){assertTrue(ExtensionRegistry.results("administrar extensiones").single().action==Action.SystemPanel("extensions"));assertTrue(ExtensionRegistry.results("texto privado cualquiera").isEmpty());assertTrue(ExtensionRegistry.resolve("apps.open")!!.action is Action.SelectMode)}
}
