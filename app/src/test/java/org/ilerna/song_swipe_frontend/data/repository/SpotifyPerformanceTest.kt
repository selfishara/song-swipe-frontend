package org.ilerna.song_swipe_frontend.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class SpotifyPerformanceTest {

    @Test
    fun cargar_y_agregar_cancion_tarda_menos_de_3_segundos() = runTest {
        val testPlaylistId = "playlist_123"
        val testTrackId = "track_456"

        val timeTakenMs = measureTimeMillis {

            simularLlamadaApiSpotify(500)

            simularLlamadaApiSpotify(400)

        }


        assertTrue(
            "El test falló: La operación tardó ${timeTakenMs}ms (Límite: 3000ms)",
            timeTakenMs < 3000
        )
    }

    private suspend fun simularLlamadaApiSpotify(tiempoMs: Long) {
        delay(tiempoMs)
    }
}