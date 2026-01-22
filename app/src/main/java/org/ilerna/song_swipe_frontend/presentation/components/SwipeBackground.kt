package org.ilerna.song_swipe_frontend.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SwipeBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Degradado aproximado al mock
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFB15A4E), // arriba (naranja/rojizo)
            Color(0xFF7A3D7E), // medio (morado)
            Color(0xFF2F93B8)  // abajo (azul)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient),
        content = content
    )
}

@Composable
fun StackedCardsBackdrop(
    modifier: Modifier = Modifier
) {
    // Dos “cards” detrás como en el mock
    Card(
        modifier = modifier
            .size(width = 260.dp, height = 340.dp)
            .rotate(-10f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3D86B5).copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {}

    Card(
        modifier = modifier
            .size(width = 260.dp, height = 340.dp)
            .rotate(8f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFCE4D7E).copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {}
}
