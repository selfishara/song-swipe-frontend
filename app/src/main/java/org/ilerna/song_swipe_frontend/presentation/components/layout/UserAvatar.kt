package org.ilerna.song_swipe_frontend.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.presentation.theme.Borders

/**
 * Reusable circular avatar component.
 * Displays user's profile image or initials fallback.
 *
 * @param user The user whose avatar to display
 * @param size Size of the avatar (diameter)
 * @param onClick Optional click handler for the avatar
 * @param modifier Modifier for the avatar
 */
@Composable
fun UserAvatar(
    user: User?,
    size: Dp = Sizes.avatarMedium,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
        .border(
            width = Borders.thin,
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        )

    if (user?.profileImageUrl != null) {
        // User has a profile image
        AsyncImage(
            model = user.profileImageUrl,
            contentDescription = "Profile picture of ${user.displayName}",
            modifier = avatarModifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // Fallback to initials
        Box(
            modifier = avatarModifier
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getInitials(user?.displayName),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Extracts initials from a display name.
 * Returns up to 2 characters (first letter of first two words).
 *
 * @param displayName The user's display name
 * @return Initials string (e.g., "JD" for "John Doe")
 */
private fun getInitials(displayName: String?): String {
    if (displayName.isNullOrBlank()) return "?"
    
    val words = displayName.trim().split(" ")
    return when {
        words.size >= 2 -> "${words[0].firstOrNull()?.uppercase() ?: ""}${words[1].firstOrNull()?.uppercase() ?: ""}"
        words.isNotEmpty() -> words[0].take(2).uppercase()
        else -> "?"
    }
}

/* PREVIEWS */
@Preview(showBackground = true)
@Composable
private fun PreviewUserAvatarWithImage() {
    SongSwipeTheme {
        UserAvatar(
            user = User(
                id = "1",
                email = "john@example.com",
                displayName = "John Doe",
                profileImageUrl = "https://example.com/avatar.jpg"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewUserAvatarWithInitials() {
    SongSwipeTheme {
        UserAvatar(
            user = User(
                id = "1",
                email = "john@example.com",
                displayName = "John Doe",
                profileImageUrl = null
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewUserAvatarNoUser() {
    SongSwipeTheme {
        UserAvatar(user = null)
    }
}
