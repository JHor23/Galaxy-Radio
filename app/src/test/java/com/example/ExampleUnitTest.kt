package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Local unit test for Galaxy Radio models and repositories.
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun curatedStations_areConfigured() {
        val stations = com.example.data.repository.RadioStationRepository.CURATED_GALAXY_STATIONS
        assertTrue(stations.isNotEmpty())
        for (station in stations) {
            assertTrue(station.streamUrl.startsWith("http://") || station.streamUrl.startsWith("https://"))
            assertTrue(station.name.isNotBlank())
        }
    }

    @Test
    fun probeCuratedStationsResponseCodes() {
        val client = okhttp3.OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val stations = com.example.data.repository.RadioStationRepository.CURATED_GALAXY_STATIONS
        for (station in stations) {
            try {
                val req = okhttp3.Request.Builder()
                    .url(station.streamUrl)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 GalaxyRadio/2.0")
                    .addHeader("Accept", "*/*")
                    .addHeader("Icy-MetaData", "1")
                    .build()
                val resp = client.newCall(req).execute()
                println("STATION ${station.name} (${station.streamUrl}): HTTP ${resp.code}")
                resp.close()
            } catch (e: Exception) {
                println("STATION ${station.name} (${station.streamUrl}): FAILED with ${e.message}")
            }
        }
    }
}
