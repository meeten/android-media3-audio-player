package com.example.mediastoreexampleapp.data.repository

import android.content.ContentResolver
import android.provider.MediaStore
import com.example.mediastoreexampleapp.data.mapper.toTrack
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAudioRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver
) : LocalAudioRepository {

    override suspend fun getTracks(): List<Track> {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<Track>()

            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION
                ),
                null,
                null,
                null
            ).use { cursor ->
                while (cursor?.moveToNext() == true) {
                    result.add(cursor.toTrack())
                }
            }

            result
        }
    }
}