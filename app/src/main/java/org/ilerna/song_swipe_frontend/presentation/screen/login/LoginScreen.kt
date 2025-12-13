package org.ilerna.song_swipe_frontend.presentation.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ilerna.song_swipe_frontend.R
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.presentation.components.AnimatedGradientBorder
import org.ilerna.song_swipe_frontend.presentation.components.PrimaryButton
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme


/**
 * Main Login Screen (UI Layer)
 * - Displays different UI based on AuthState
 * - Hides the logo entirely when an error occurs (full-screen error UI)
 */
@Composable
fun LoginScreen(
    authState: AuthState,
    onLoginClick: () -> Unit,
    onResetState: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Animated neon border around the whole screen
        AnimatedGradientBorder(
            modifier = Modifier
                .matchParentSize()
                .padding(2.dp),
            strokeWidth = Sizes.borderStrokeWidth,
            cornerRadius = Sizes.borderCornerRadius
        )

        // If error → show only the full-screen error UI
        if (authState is AuthState.Error) {
            LoginScreenError(
                errorMessage = authState.message, onNavigateBack = onResetState
            )
        } else {
            // Normal login UI (logo + states)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Logo shown only when NOT in error state
                Image(
                    painter = painterResource(id = R.drawable.songswipe_logo),
                    contentDescription = "SongSwipe Logo",
                    modifier = Modifier.size(170.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                //  UI STATE HANDLING
                when (authState) {

                    is AuthState.Idle -> {
                        Text(
                            text = "Swipe to discover new music!",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        PrimaryButton(
                            text = "Continue with Spotify", onClick = onLoginClick
                        )
                    }

                    is AuthState.Loading -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                    is AuthState.Success -> {
                        SuccessContent(
                            authorizationCode = authState.authorizationCode
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}


/*  SUCCESS STATE COMPONENT */
@Composable
private fun SuccessContent(
    authorizationCode: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = "Login successfully",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Authorization Code:", style = MaterialTheme.typography.labelMedium
        )

        Text(
            text = authorizationCode,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/* PREVIEWS */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginIdle() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Idle, onLoginClick = {}, onResetState = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginLoading() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Loading, onLoginClick = {}, onResetState = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginError() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Error("Login failed"), onLoginClick = {}, onResetState = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginSuccess() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Success("code123"), onLoginClick = {}, onResetState = {})
    }
}
