package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.Track
import com.example.ui.extension.toTimeString

@Composable
fun TracksScreenContent(
    modifier: Modifier = Modifier,
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onDownload: (Track) -> Unit
) {
    LazyColumn(modifier) {
        items(items = tracks, key = { it.id }) { track ->
            TrackCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onTrackClick(track) },
                imageUrl = track.smallImageUrl,
                title = track.title,
                artist = track.artist,
                duration = track.durationMs.toTimeString(),
                isDownload = track.isDownload,
                isProcessing = track.isDownloading,
                onDownload = { onDownload(track) }
            )
        }
    }
}