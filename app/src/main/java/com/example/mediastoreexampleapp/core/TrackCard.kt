package com.example.mediastoreexampleapp.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediastoreexampleapp.R

@Composable
fun TrackCard(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    title: String,
    artist: String,
    duration: String,
    isDownload: Boolean,
    isProcessing: Boolean,
    onDownload: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(width = 1.dp, color = Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoadingImage(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(shape = RoundedCornerShape(16.dp)),
                    imageUrl = imageUrl,
                    loadingProgressIndicatorSize = 48.dp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = artist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = duration)

            Spacer(modifier = Modifier.width(8.dp))

            if (isProcessing) {
                Loading(modifier = Modifier.size(24.dp))
            } else {
                Icon(
                    painter =
                        if (isDownload) painterResource(R.drawable.ic_check_circle)
                        else painterResource(R.drawable.ic_download),
                    contentDescription = null,
                    modifier = Modifier.clickable { onDownload() },
                    tint = if (isDownload) Color.Green else Color.Gray,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrackCardPreview() {
    TrackCard(
        modifier = Modifier.padding(8.dp),
        imageUrl = null,
        title = "Starboy",
        artist = "The Weeknd ft. Daft Punk",
        duration = "03:50",
        isDownload = false,
        isProcessing = false,
        onDownload = {}
    )
}

@Preview(showBackground = true)
@Composable
fun TrackCardAlbumArtPreview() {
    TrackCard(
        modifier = Modifier.padding(8.dp),
        imageUrl = null,
        title = "Blinding Lights",
        artist = "The Weeknd",
        duration = "03:22",
        isDownload = false,
        isProcessing = true,
        onDownload = {}
    )
}

@Preview(showBackground = true)
@Composable
fun TrackCardLongTextPreview() {
    TrackCard(
        modifier = Modifier.padding(8.dp),
        imageUrl = null,
        title = "Очень длинное название трека, которое точно не поместится в одну строку",
        artist = "Исполнитель с очень длинным именем и дополнительными словами",
        duration = "10:59",
        isDownload = true,
        isProcessing = false,
        onDownload = {}
    )
}