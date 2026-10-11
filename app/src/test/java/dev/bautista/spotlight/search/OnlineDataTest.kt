package dev.bautista.spotlight.search

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class OnlineDataTest {
    @Test
    fun currencyShowsProviderDateAndCalculatedValue() {
        val card =
            OnlineTools.parseCurrency(
                JSONObject("""{"date":"2026-10-08","rate":0.9}"""),
                100.0,
                "USD",
                "EUR",
            )
        assertEquals("90 EUR", card.title)
        assertEquals("90", card.copyText)
        assertTrue(card.subtitle.contains("2026-10-08"))
        assertTrue(card.body.contains("referencia diaria"))
    }

    @Test
    fun weatherIncludesHourlyAndDailyDataAndFullPlace() {
        val data =
            JSONObject(
                """{"timezone":"America/Argentina/Buenos_Aires","current":{"time":"2026-10-09T15:00","temperature_2m":19.5,"apparent_temperature":18,"relative_humidity_2m":60,"weather_code":2,"wind_speed_10m":12},"hourly":{"time":["2026-10-09T15:00","2026-10-09T16:00"],"temperature_2m":[19.5,18],"precipitation_probability":[20,40]},"daily":{"time":["2026-10-09"],"temperature_2m_min":[12],"temperature_2m_max":[21],"weather_code":[2]}}"""
            )
        val card = OnlineTools.parseWeather(data, "Mar del Plata, Argentina", -38.0, -57.5)
        assertTrue(card.title.contains("19.5"))
        assertEquals(3, card.rows.size)
        assertEquals("Mar del Plata, Argentina", card.subtitle)
        assertTrue(card.rows[1].second.contains("40%"))
        assertTrue(card.url!!.contains("-38.0,-57.5"))
    }

    @Test
    fun recognizesOnlyCompleteRequests() {
        val tools = OnlineTools()
        assertEquals("currency", tools.request("100 USD a EUR")?.type)
        assertNull(tools.request("10 km a mi"))
        assertEquals("weather", tools.request("clima en Mar del Plata")?.type)
        assertEquals("flight", tools.request("AR1301")?.type)
    }
}
