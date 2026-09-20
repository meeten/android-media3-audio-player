package com.example.mediastoreexampleapp.data.repository

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
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
                    MediaStore.Audio.Media.AUTHOR,
                    MediaStore.Audio.Media.DURATION
                ),
                null,
                null,
                null
            ).use { cursor ->
                while (cursor?.moveToNext() == true) {
                    val id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    )
                    val title = cursor.getStringOrNull(
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    )
                    val author = cursor.getStringOrNull(
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.AUTHOR)
                    )
                    val duration = cursor.getLongOrNull(
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    )

                    result.add(
                        Track(
                            id = id,
                            title = title,
                            author = author,
                            duration = duration
                        )
                    )
                }
            }

            result
        }
    }
}