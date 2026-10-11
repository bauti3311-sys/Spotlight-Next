package dev.bautista.spotlight.search

import android.content.Intent
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class ResultSectionsTest {
    private fun row(id: String, category: String, score: Int) = Result(id,id,"",category,score,Action.Copy(id))
    @Test fun manyFilesCannotDisplaceAppAndSectionsAreLimited() {
        val rows=(1..80).map { row("file:$it","Archivos",9000-it) } + row("app","Aplicaciones",800) + row("command","Acciones",500)
        val displayed=ResultSections.grouped(rows)
        assertEquals("app",displayed.first().id)
        assertEquals("command",displayed[1].id)
        assertEquals(3,displayed.count { it.category=="Archivos" })
        assertTrue(displayed.last().action is Action.Navigate)
        assertEquals(82,ResultSections.grouped(rows,expanded=true).size)
    }
    @Test fun metadataSeparatesSettingsWeatherSportAndMessaging() {
        assertEquals("Clima",ResultSections.category(row("w","Información",1).copy(action=Action.Lookup("weather","Buenos Aires"))))
        assertEquals("Deportes en vivo",ResultSections.category(row("s","Información",1).copy(action=Action.Lookup("sports","Boca"))))
        assertEquals("Ajustes del dispositivo",ResultSections.category(row("b","Acciones",1).copy(action=Action.Launch(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)))))
        assertEquals("Contactos y comunicación",ResultSections.category(row("m","Acciones",1).copy(action=Action.ComposeMessage("whatsapp","Juan"))))
    }
    @Test fun naturalYesterdayPhotosUsesDateFilterRatherThanFilename() {
        val q=SearchQuery.parse(SearchQuery.natural("buscar fotos de ayer"))
        val yesterday=java.time.LocalDate.now().minusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertTrue(q.matches(SearchQuery.Document("IMG_123.jpg","image",created=yesterday)))
        assertFalse(q.matches(SearchQuery.Document("ayer.jpg","image",created=System.currentTimeMillis())))
    }
}
