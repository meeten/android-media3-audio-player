package com.example.mediastoreexampleapp.presentation.tracks.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mediastoreexampleapp.domain.model.Track

@Composable
fun TracksScreenContent(
    modifier: Modifier = Modifier,
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit
) {
    LazyColumn(modifier) {
        items(items = tracks, key = { it.id }) { track ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        onTrackClick(track)
                    },
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = track.title)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = track.id.toString())
                }
            }
        }
    }
}