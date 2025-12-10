package org.ilerna.song_swipe_frontend.presentation.screen.login

import GradienteNeon
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ilerna.song_swipe_frontend.R
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.presentation.components.PrimaryButton
/**
 * Main Login Screen (UI Layer)
 * - Displays different UI based on AuthState
 * - Hides the logo entirely when an error occurs (full-screen error UI)
 */
@Composable
fun LoginScreen(
    authState: AuthState,
    onLoginClick: () -> Unit,
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
            strokeWidth = 3.dp,
            cornerRadius = 38.dp
        )

        // If error → show only the full-screen error UI
        if (authState is AuthState.Error) {
            LoginScreenError(
                errorMessage = authState.message,
                onNavigateBack = onLoginClick
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

                // 🔵 Logo shown only when NOT in error state
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
                            text = "Continue with Spotify",
                            onClick = onLoginClick
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

                    else -> {
                        // AuthState.Error is already handled above, so nothing to do here.
                    }
                }
            }
        }
    }
}


/*  ANIMATED NEON BORDER COMPOSABLE */
@Composable
fun AnimatedGradientBorder(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp,
    cornerRadius: Dp = 38.dp
) {
    val transition = rememberInfiniteTransition(label = "borderTransition")

    // Offset animation to simulate gradient movement
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetAnim"
    )

    Canvas(modifier = modifier) {

        // Neon gradient
        val brush = Brush.linearGradient(
            colors = GradienteNeon + GradienteNeon.first(), // closes the loop
            start = Offset(-size.width + offset, 0f),
            end = Offset(offset, size.height)
        )

        // Make sure corner radius never exceeds half the screen
        val radiusPx = cornerRadius
            .toPx()
            .coerceAtMost(size.minDimension / 2f)

        drawRoundRect(
            brush = brush,
            style = Stroke(width = strokeWidth.toPx()),
            cornerRadius = CornerRadius(radiusPx, radiusPx)
        )
    }
}

/*  SUCCESS STATE COMPONENT */
@Composable
private fun SuccessContent(
    authorizationCode: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = "Login succesfully",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

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

/* PREVIEWS */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginIdle() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Idle,
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginLoading() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Loading,
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginError() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Error("Login failed"),
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginSuccess() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Success("code123"),
            onLoginClick = {}
        )
    }
}
