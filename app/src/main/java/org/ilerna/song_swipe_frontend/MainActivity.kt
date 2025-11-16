package org.ilerna.song_swipe_frontend

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.ilerna.song_swipe_frontend.ui.theme.SongswipefrontendTheme

class MainActivity : ComponentActivity() {

    private var authorizationCode by mutableStateOf<String?>(null)
    private var errorMessage by mutableStateOf<String?>(null)
    private var isLoggedIn by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check if we're being called back from Spotify auth
        handleIntent(intent)
        
        setContent {
            SongswipefrontendTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding),
                        isLoggedIn = isLoggedIn,
                        authorizationCode = authorizationCode,
                        errorMessage = errorMessage,
                        onLoginClick = { initiateSpotifyLogin() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null) {
            Log.d(AppConfig.LOG_TAG, "Received callback URI: $uri")
            val response = AuthorizationResponse.fromUri(uri)

            when (response.type) {
                AuthorizationResponse.Type.CODE -> {
                    authorizationCode = response.code
                    isLoggedIn = true
                    errorMessage = null
                    Log.d(AppConfig.LOG_TAG, "Authorization successful! Code: ${response.code}")
                }
                AuthorizationResponse.Type.ERROR -> {
                    errorMessage = "Error: ${response.error}"
                    isLoggedIn = false
                    Log.e(AppConfig.LOG_TAG, "Authorization error: ${response.error}")
                }
                else -> {
                    errorMessage = "Authorization cancelled"
                    isLoggedIn = false
                    Log.d(AppConfig.LOG_TAG, "Authorization cancelled")
                }
            }
        }
    }

    private fun initiateSpotifyLogin() {
        val builder = AuthorizationRequest.Builder(
            AppConfig.SPOTIFY_CLIENT_ID,
            AuthorizationResponse.Type.CODE,
            AppConfig.SPOTIFY_REDIRECT_URI
        )

        builder.setScopes(AppConfig.SPOTIFY_SCOPES)
        val request = builder.build()

        AuthorizationClient.openLoginActivity(this, AppConfig.AUTH_REQUEST_CODE, request)
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean,
    authorizationCode: String?,
    errorMessage: String?,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Song Swipe",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        if (!isLoggedIn) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = "Login with Spotify")
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            Text(
                text = "✓ Successfully logged in!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            if (authorizationCode != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Authorization Code:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = authorizationCode.take(20) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SongswipefrontendTheme {
        LoginScreen(
            isLoggedIn = false,
            authorizationCode = null,
            errorMessage = null,
            onLoginClick = {}
        )
    }
}