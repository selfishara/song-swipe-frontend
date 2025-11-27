package org.ilerna.song_swipe_frontend.ui.screen.login

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ilerna.song_swipe_frontend.R
import org.ilerna.song_swipe_frontend.ui.theme.SongswipefrontendTheme

/**
 * Composable that displays an error after a failed LOGIN attempt.
 * Uses direct hexadecimal color codes (temporarily).
 */
@Composable
fun LoginScreenError(
    errorMessage: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Color definitions using hexadecimal values until the palette is uploaded (0xFF + RGB code)
    val ColorGrisProfundo = Color(0xFF1A1A1A)       // Dark background
    val ColorCianIntenso = Color(0xFF00FFFF)        // Neon Cyan
    val ColorRosaNeonIntenso = Color(0xFFFF00FF)    //Neon Pink / Magenta
    val ColorLavanda = Color(0xFF8A2BE2)          // Lavender / Purple

    val vibrantGradient = Brush.horizontalGradient(
        colors = listOf(ColorCianIntenso, ColorRosaNeonIntenso, ColorLavanda)
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ColorGrisProfundo // Dark background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
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
            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Oh! Something went wrong...",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            Text(
                text = if (errorMessage.isNotEmpty()) {
                    errorMessage
                } else {
                    "We couldn't complete your login request. Please try again or contact support."
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.LightGray,
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            Spacer(modifier = Modifier.height(48.dp))

            // "Back to Login" button with gradient background
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(vibrantGradient, MaterialTheme.shapes.extraLarge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent, // Transparent so the gradient is visible
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Back to Login",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// Preview for Android Studio@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun LoginScreenErrorPreview() {
    SongswipefrontendTheme {
        LoginScreenError(
            errorMessage = "The provided credentials do not match our records.",
            onNavigateBack = {}
        )
    }
}