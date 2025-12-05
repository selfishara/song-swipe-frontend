package org.ilerna.song_swipe_frontend.presentation.screen.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme

/**
 * Login screen composable that displays the authentication UI
 */
@Composable
fun LoginScreen(
    authState: AuthState,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
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

        when (authState) {
            is AuthState.Idle -> {
                LoginButton(onLoginClick = onLoginClick)
            }
            is AuthState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthState.Success -> {
                SuccessContent(authorizationCode = authState.authorizationCode)
            }
            is AuthState.Error -> {
                LoginButton(onLoginClick = onLoginClick)
                ErrorMessage(message = authState.message)
            }
        }
    }
}

@Composable
private fun LoginButton(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onLoginClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(text = "Login with Spotify")
    }
}

@Composable
private fun SuccessContent(
    authorizationCode: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✓ Successfully logged in",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Authorization Code:",
            style = MaterialTheme.typography.labelMedium
        )
        
        Text(
            text = authorizationCode,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.padding(top = 16.dp)
    )
}

// Preview composables
@Preview(showBackground = true)
@Composable
fun LoginScreenIdlePreview() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Idle,
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Loading,
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenSuccessPreview() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Success("sample_authorization_code_12345"),
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenErrorPreview() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Error("Authentication failed. Please try again."),
            onLoginClick = {}
        )
    }
}
