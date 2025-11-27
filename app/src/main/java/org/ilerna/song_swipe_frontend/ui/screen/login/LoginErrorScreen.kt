package org.ilerna.song_swipe_frontend.ui.screen.login

import GradienteNeon
import GrisProfundo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ilerna.song_swipe_frontend.R
import org.ilerna.song_swipe_frontend.ui.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.ui.theme.Sizes
import org.ilerna.song_swipe_frontend.ui.theme.Spacing

/**
 * Composable that displays an error after a failed LOGIN attempt.
 * Uses the SongSwipe design system (palette + typography + spacing).
 */
@Composable
fun LoginScreenError(
    errorMessage: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vibrantGradient = Brush.horizontalGradient(
        colors = GradienteNeon
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = GrisProfundo // Dark background from the SongSwipe palette
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // PNG Image
            Image(
                painter = painterResource(id = R.drawable.audio_waves),
                contentDescription = "Error Indicator",
                modifier = Modifier
                    .size(120.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.spaceXl))

            // Title
            Text(
                text = "Oh! Something went wrong...",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.spaceMd))

            // Error message
            Text(
                text = if (errorMessage.isNotEmpty()) {
                    errorMessage
                } else {
                    "We couldn't complete your login request. Please try again or contact support."
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            Spacer(modifier = Modifier.height(Spacing.spaceXl))

            // "Back to Login" button with gradient background
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Sizes.buttonHeight)
                    .background(vibrantGradient, MaterialTheme.shapes.extraLarge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent, // Transparent so the gradient is visible
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(
                    horizontal = Spacing.spaceLg,
                    vertical = Spacing.spaceSm
                )
            ) {
                Text(
                    text = "Back to Login",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// Preview for Android Studio
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun LoginErrorScreenPreview() {
    SongSwipeTheme {
        LoginScreenError(
            errorMessage = "The provided credentials do not match our records.",
            onNavigateBack = {}
        )
    }
}
