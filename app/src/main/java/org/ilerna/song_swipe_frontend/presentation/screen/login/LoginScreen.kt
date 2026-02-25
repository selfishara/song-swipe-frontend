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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import org.ilerna.song_swipe_frontend.R
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.model.UserProfileState
import org.ilerna.song_swipe_frontend.presentation.components.animation.AnimatedGradientBorder
import org.ilerna.song_swipe_frontend.presentation.components.PrimaryButton
import org.ilerna.song_swipe_frontend.presentation.theme.Borders
import org.ilerna.song_swipe_frontend.presentation.theme.Radius
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing


/**
 * Main Login Screen (UI Layer)
 * - Displays different UI based on AuthState
 * - Hides the logo entirely when an error occurs (full-screen error UI)
 */
@Composable
fun LoginScreen(
    authState: AuthState,
    userProfileState: UserProfileState = UserProfileState.Idle,
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
                .padding(Borders.thin),
            strokeWidth = Borders.medium,
            cornerRadius = Radius.pill
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
                    .padding(horizontal = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                //  UI STATE HANDLING
                when (authState) {

                    is AuthState.Idle -> {
                        // Logo shown in Idle state
                        Image(
                            painter = painterResource(id = R.drawable.songswipe_logo),
                            contentDescription = "SongSwipe Logo",
                            modifier = Modifier.size(Sizes.logoLarge)
                        )

                        Spacer(modifier = Modifier.height(Spacing.xl))

                        Text(
                            text = "Swipe to discover new music!",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(Spacing.xxl))

                        PrimaryButton(
                            text = "Continue with Spotify", onClick = onLoginClick
                        )
                    }

                    is AuthState.Loading -> {
                        // Logo shown in Loading state
                        Image(
                            painter = painterResource(id = R.drawable.songswipe_logo),
                            contentDescription = "SongSwipe Logo",
                            modifier = Modifier.size(Sizes.logoLarge)
                        )

                        Spacer(modifier = Modifier.height(Spacing.xl))

                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                    is AuthState.Success -> {
                        SuccessContent(userProfileState = userProfileState)
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
    userProfileState: UserProfileState
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Small SongSwipe logo at the top
        Image(
            painter = painterResource(id = R.drawable.songswipe_logo),
            contentDescription = "SongSwipe Logo",
            modifier = Modifier.size(Sizes.iconXLarge)
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        when (userProfileState) {
            is UserProfileState.Loading -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            is UserProfileState.Success -> {
                val user = userProfileState.user

                // Profile image
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(Sizes.logoMedium)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Welcome message
                Text(
                    text = "Welcome,",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = user.displayName,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            is UserProfileState.Error -> {
                Text(
                    text = "Welcome!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "Could not load profile",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            is UserProfileState.Idle -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/* PREVIEWS */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginIdle() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Idle,
            onLoginClick = {},
            onResetState = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginLoading() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Loading,
            onLoginClick = {},
            onResetState = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginError() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Error("Login failed"),
            onLoginClick = {},
            onResetState = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginSuccess() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Success("code123"),
            userProfileState = UserProfileState.Success(
                User(
                    id = "123",
                    email = "user@example.com",
                    displayName = "Federico Sánchez",
                    profileImageUrl = "https://i.scdn.co/image/ab6775700000ee856a2a4c0754ecf2c5f3bf2e8e"
                )
            ),
            onLoginClick = {},
            onResetState = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginSuccessLoading() {
    SongSwipeTheme {
        LoginScreen(
            authState = AuthState.Success("code123"),
            userProfileState = UserProfileState.Loading,
            onLoginClick = {},
            onResetState = {}
        )
    }
}
