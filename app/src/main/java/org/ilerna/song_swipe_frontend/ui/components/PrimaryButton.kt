package org.ilerna.song_swipe_frontend.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.ilerna.song_swipe_frontend.ui.theme.Inter
import org.ilerna.song_swipe_frontend.ui.theme.Sizes

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFF69752),
            Color(0xFFF35987),
            Color(0xFF736FE7),
            Color(0xFF20CBD1)
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 0f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Sizes.buttonHeight)
            .clip(MaterialTheme.shapes.medium)
            .background(gradient)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.wrapContentHeight()
        )
    }
}

