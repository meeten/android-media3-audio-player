package com.example.mediastoreexampleapp.data.mapper

import android.content.ContentUris
import android.database.Cursor
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.example.mediastoreexampleapp.domain.model.Track

fun Cursor.toTrack(): Track {
    val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
    val title = getStringOrNull(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
    val artist = getStringOrNull(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
    val duration = getLongOrNull(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

    return Track(
        id = id,
        title = title,
        artist = artist,
        duration = duration,
        uri = uri
    )
}