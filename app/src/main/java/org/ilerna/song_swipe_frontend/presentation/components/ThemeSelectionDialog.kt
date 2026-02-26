package org.ilerna.song_swipe_frontend.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.ThemeMode
import org.ilerna.song_swipe_frontend.presentation.theme.Radius
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing

/**
 * Dialog for selecting the app theme (Light, Dark, System).
 *
 * Displays a list of radio buttons for each [ThemeMode] option.
 * The user can select one and confirm with "Apply" or dismiss with "Cancel".
 *
 * @param currentTheme The currently active [ThemeMode]
 * @param onThemeSelected Callback when the user confirms a new theme selection
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTheme by remember { mutableStateOf(currentTheme) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Radius.large),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg)
            ) {
                // Title
                Text(
                    text = "Choose Theme",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                // Theme options
                Column(modifier = Modifier.selectableGroup()) {
                    ThemeMode.entries.forEach { themeMode ->
                        ThemeOptionRow(
                            themeMode = themeMode,
                            isSelected = selectedTheme == themeMode,
                            onSelect = { selectedTheme = themeMode }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(Spacing.sm))

                    TextButton(
                        onClick = {
                            onThemeSelected(selectedTheme)
                            onDismiss()
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

/**
 * Single row for a theme option with radio button.
 */
@Composable
private fun ThemeOptionRow(
    themeMode: ThemeMode,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null, // handled by selectable modifier
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.width(Spacing.md))

        Text(
            text = themeMode.getDisplayName(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/* PREVIEWS */

@Preview(showBackground = true)
@Composable
private fun PreviewThemeSelectionDialog() {
    SongSwipeTheme {
        ThemeSelectionDialog(
            currentTheme = ThemeMode.SYSTEM,
            onThemeSelected = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewThemeSelectionDialogLight() {
    SongSwipeTheme(darkTheme = false) {
        ThemeSelectionDialog(
            currentTheme = ThemeMode.LIGHT,
            onThemeSelected = {},
            onDismiss = {}
        )
    }
}
