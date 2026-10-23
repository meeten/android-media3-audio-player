package com.example.mediastoreexampleapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.mediastoreexampleapp.data.mapper.toTrack
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class LocalAudioRepositoryImpl @Inject constructor(
    @param:ApplicationContext val context: Context,
    coroutineScope: CoroutineScope
) : LocalAudioRepository {

    private val audioColumns = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.ALBUM_ID
    )

    override val tracksFlow: StateFlow<List<Track>?> = callbackFlow {
        val callback = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                trySend(Unit)
            }
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            callback
        )

        awaitClose {
            context.contentResolver.unregisterContentObserver(callback)
        }
    }.onStart {
        emit(Unit)
    }.mapLatest {
        loadTracks()
    }.flowOn(Dispatchers.IO).stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    override suspend fun insert(track: Track): Boolean = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "${track.title}.mp3")
            put(MediaStore.Audio.Media.TITLE, track.title)
            put(MediaStore.Audio.Media.MIME_TYPE, MIME_TYPE)
            put(MediaStore.Audio.Media.ARTIST, track.artist)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Audio.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_MUSIC}/$MY_MUSIC"
                )
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }

        var uri: Uri? = null
        return@withContext try {
            uri = context.contentResolver.insert(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: return@withContext false

            //TODO: change to retrofit
            URL(track.trackUri).openStream().use { input ->
                context.contentResolver.openOutputStream(uri)
                    ?.use { output -> copyAudio(input, output) }
                    ?: throw IOException("Output stream is null")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.IS_PENDING, 0)
                }
                context.contentResolver.update(uri, values, null, null)
            }
            true
        } catch (e: CancellationException) {
            uri?.let { context.contentResolver.delete(uri, null, null) }
            throw e
        } catch (_: Exception) {
            uri?.let { context.contentResolver.delete(uri, null, null) }
            false
        }
    }

    override fun remove(track: Track): Boolean {
        return false
    }

    private fun loadTracks(): List<Track> {
        return context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            audioColumns,
            null,
            null
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.toTrack())
            }
        } ?: emptyList()
    }

    private fun copyAudio(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(BUFFER_CAPACITY)

        while (true) {
            val count = input.read(buffer)

            if (count == -1) break

            output.write(buffer, 0, count)
        }
    }

    companion object {

        private const val MIME_TYPE = "audio/mpeg"
        private const val MY_MUSIC = "my_music"
        private const val BUFFER_CAPACITY = 8 * 1024
    }
}