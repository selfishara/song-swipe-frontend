package org.ilerna.song_swipe_frontend.core.network.interceptors

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class SpotifyRetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: Exception? = null
        var tryCount = 0
        val maxRetries = 3

        while (tryCount < maxRetries && (response == null || !response.isSuccessful)) {
            // 401 is handled by the SpotifyAuthenticator (token refresh flow);
            // retrying here just wastes calls and delays the auth recovery.
            if (response?.code == 401) {
                break
            }

            try {
                response?.close()

                if (tryCount > 0) {
                    Log.d("SpotifyAPI", "Retry $tryCount for: ${request.url}")
                }

                response = chain.proceed(request)
            } catch (e: Exception) {
                exception = e
                Log.e("SpotifyAPI", "Connection failure on attempt $tryCount", e)
            } finally {
                tryCount++
            }
        }

        return response ?: throw exception ?: IOException("Request failed after $maxRetries attempts")
    }
}