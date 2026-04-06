package org.ilerna.song_swipe_frontend.core.network.interceptors


import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import org.ilerna.song_swipe_frontend.core.analytics.AnalyticsManager

/**
 * Interceptor to measure API response times in the network layer.
 * Fulfills the requirement: "Medir el tiempo de respuesta... dentro de la capa de red"
 * and "Registrar casos donde se excedan los tiempos".
 */
class SpotifyPerformanceInterceptor(
    private val analyticsManager: AnalyticsManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Empezamos el cronómetro
        val startTime = System.currentTimeMillis()

        // Ejecutamos la petición
        val response = chain.proceed(request)

        // Paramos el cronómetro y calculamos
        val duration = System.currentTimeMillis() - startTime
        val method = request.method
        val endpoint = request.url.encodedPath
        Log.d("TEST_RENDIMIENTO", "⏱️ [$method] Petición a $endpoint tardó: ${duration}ms")
        if (duration > 500) {
            val endpoint = request.url.encodedPath // Sacamos solo la ruta (ej: /v1/me)
            analyticsManager.logSlowApiResponse(endpoint, duration)
        }

        return response
    }
}