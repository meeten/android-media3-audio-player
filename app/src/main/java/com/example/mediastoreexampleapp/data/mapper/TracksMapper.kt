package com.example.mediastoreexampleapp.data.mapper

import android.content.ContentUris
import android.database.Cursor
import android.provider.MediaStore
import com.example.mediastoreexampleapp.data.network.model.TrackDto
import com.example.mediastoreexampleapp.domain.model.Track

fun Cursor.toTrack(): Track {
    val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
    val title = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
    val artist = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
    val duration = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

    return Track(
        id = id,
        title = title,
        artist = artist,
        duration = duration,
        uri = uri.toString()
    )
}

fun TrackDto.toTrack(): Track {
    return Track(
        id = id,
        title = title,
        duration = duration,
        artist = artistDto.name ?: UNKNOWN_ARTIST,
        uri = preview,
        isPreview = true
    )
}

private const val UNKNOWN_ARTIST = "Unknown"