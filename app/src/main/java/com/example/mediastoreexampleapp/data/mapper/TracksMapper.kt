package com.example.mediastoreexampleapp.data.mapper

import android.content.ContentUris
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import androidx.core.net.toUri
import com.example.mediastoreexampleapp.data.network.model.TrackDto
import com.example.mediastoreexampleapp.domain.model.Track

fun Cursor.toTrack(): Track {
    val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
    val title = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
    val artist = getString(
        getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
    )?.takeUnless { it == MediaStore.UNKNOWN_STRING }
        ?: UNKNOWN_ARTIST
    val duration = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
    val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    val album = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
    val albumId = getLongOrNull(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
    val imageUri =
        if (albumId == null || albumId <= 0) null
        else ContentUris.withAppendedId(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
            } else {
                "content://media/external/audio/albumart".toUri()
            },
            albumId
        )
    return Track(
        id = id,
        title = title,
        artist = artist,
        durationMs = duration,
        trackUri = trackUri.toString(),
        imageUrl = imageUri?.toString(),
        isPreview = false,
        isDownload = true,
        localCopyUri = trackUri.toString(),
        sourceRemoteId = album?.takeIf { it.startsWith("MediaStoreExampleApp track ") }
            ?.removePrefix("MediaStoreExampleApp track ")?.toLongOrNull(),
    )
}

fun TrackDto.toTrack(): Track {
    return Track(
        id = id,
        title = title,
        durationMs = durationSeconds * MILLIS_PER_SECOND,
        artist = artistDto.name ?: UNKNOWN_ARTIST,
        trackUri = preview,
        imageUrl = artistDto.imageUrl,
        isPreview = true,
        isDownload = false
    )
}

private const val UNKNOWN_ARTIST = "Unknown"
private const val MILLIS_PER_SECOND = 1_000