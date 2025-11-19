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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ilerna.song_swipe_frontend.R
import org.ilerna.song_swipe_frontend.ui.theme.SongswipefrontendTheme

/**
 * Composable que muestra un error después de un intento de LOGIN fallido.
 * Utiliza códigos de color hexadecimales directos.
 */
@Composable
fun LoginScreenError(
    errorMessage: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Definición de Colores con Hexadecimales hasta que se suba la paleta (0xFF + código RGB)
    val ColorGrisProfundo = Color(0xFF1A1A1A)       // Fondo oscuro
    val ColorCianIntenso = Color(0xFF00FFFF)        // Cian Neón
    val ColorRosaNeonIntenso = Color(0xFFFF00FF)    // Rosa Neón / Magenta
    val ColorLavanda = Color(0xFF8A2BE2)          // Lavanda / Morado

    val vibrantGradient = Brush.horizontalGradient(
        colors = listOf(ColorCianIntenso, ColorRosaNeonIntenso, ColorLavanda)
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ColorGrisProfundo // Fondo oscuro
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Imagen png
            Image(
                painter = painterResource(id = R.drawable.audio_waves),
                contentDescription = "Error Indicator",
                modifier = Modifier
                    .size(120.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Título
            Text(
                text = "Uh Oh! Something went wrong...",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje de error
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

            //  Botón "Back to Login" con degradado
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(vibrantGradient, MaterialTheme.shapes.extraLarge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent, // Fondo transparente para que se vea el degradado del modifier
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

// Preview para Android Studio
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun LoginScreenErrorPreview() {
    SongswipefrontendTheme {
        LoginScreenError(
            errorMessage = "The provided credentials do not match our records.",
            onNavigateBack = {}
        )
    }
}