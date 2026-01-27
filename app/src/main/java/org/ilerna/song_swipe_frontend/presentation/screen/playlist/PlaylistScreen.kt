package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Simple screen to search Spotify playlists by genre (category).
 * Intended for debug/manual verification of the feature.
 */
@Composable
fun PlaylistsScreen(
    viewModel: PlaylistViewModel
) {
    val state by viewModel.state.collectAsState()
    var genre by remember { mutableStateOf("pop") }

    Column(modifier = Modifier.padding(16.dp)) {

        Text(text = "Playlists by genre")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = genre,
            onValueChange = { genre = it },
            label = { Text("Genre (e.g. pop, rock)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { viewModel.load(genre) }) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val s = state) {
            is PlaylistsState.Idle -> {
                Text("Enter a genre and press Search.")
            }
            is PlaylistsState.Loading -> {
                CircularProgressIndicator()
            }
            is PlaylistsState.Error -> {
                Text("Error: ${s.message}")
            }
            is PlaylistsState.Success -> {
                Text("Results: ${s.playlists.size}")
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(s.playlists) { p ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(text = p.name)
                            if (!p.description.isNullOrBlank()) {
                                Text(text = p.description ?: "")
                            }
                        }
                    }
                }
            }
        }
    }
}