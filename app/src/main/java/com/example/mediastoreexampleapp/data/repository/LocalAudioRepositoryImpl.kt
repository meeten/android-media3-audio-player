package com.example.mediastoreexampleapp.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
    @param:ApplicationContext private val context: Context,
    coroutineScope: CoroutineScope
) : LocalAudioRepository {

    override val tracksFlow: StateFlow<List<Track>?> =
        callbackFlow {
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    trySend(Unit)
                }
            }

            context.contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )

            awaitClose {
                context.contentResolver.unregisterContentObserver(observer)
            }
        }
            .onStart { emit(Unit) }
            .mapLatest { loadTracks() }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    override suspend fun insert(track: Track): Boolean =
        withContext(Dispatchers.IO) {
            val uri = createAudioUri(track) ?: return@withContext false

            try {
                downloadAudio(track, uri)
                publishAudio(uri)
                true
            } catch (e: CancellationException) {
                deleteAudio(uri)
                throw e
            } catch (e: Exception) {
                Log.e("LocalAudioRepository", "Failed to download ${track.title}", e)
                deleteAudio(uri)
                false
            }
        }

    private fun createAudioUri(track: Track): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "${track.title}.mp3")
            put(MediaStore.Audio.Media.TITLE, track.title)
            put(MediaStore.Audio.Media.ARTIST, track.artist)
            put(MediaStore.Audio.Media.MIME_TYPE, MIME_TYPE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Audio.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_MUSIC}/$MY_MUSIC"
                )
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }

        return context.contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            values
        )
    }

    private fun downloadAudio(track: Track, uri: Uri) {
        URL(track.trackUri).openStream().use { input ->
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(buildId3Tag(track.title, track.artist))
                copyAudio(input, output)
            } ?: throw IOException("Cannot open output stream")
        }
    }

    private fun publishAudio(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.IS_PENDING, 0)
            }

            context.contentResolver.update(uri, values, null, null)
        }
    }

    private fun deleteAudio(uri: Uri) {
        context.contentResolver.delete(uri, null, null)
    }

    override fun remove(track: Track): Boolean {
        val uri = resolveLocalUri(track) ?: return false
        return try {
            context.contentResolver.delete(uri, null, null) > 0
        } catch (e: Exception) {
            Log.e("LocalAudioRepository", "Failed to delete ${track.title}", e)
            false
        }
    }

    private fun loadTracks(): List<Track> {
        return context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            AUDIO_COLUMNS,
            null,
            null,
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toTrack())
                }
            }
        } ?: emptyList()
    }

    private fun copyAudio(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(BUFFER_SIZE)

        while (true) {
            val count = input.read(buffer)
            if (count == -1) break
            output.write(buffer, 0, count)
        }
    }

    private fun buildId3Tag(title: String, artist: String): ByteArray {
        val frames = listOf(
            buildTextFrame("TIT2", title),
            buildTextFrame("TPE1", artist)
        )

        val bodySize = frames.sumOf { it.size }
        val tag = ByteArray(10 + bodySize)

        tag[0] = 'I'.code.toByte()
        tag[1] = 'D'.code.toByte()
        tag[2] = '3'.code.toByte()
        tag[3] = 3
        tag[4] = 0
        tag[5] = 0
        writeSyncSafeInt(tag, 6, bodySize)

        var offset = 10
        frames.forEach { frame ->
            frame.copyInto(tag, offset)
            offset += frame.size
        }

        return tag
    }

    private fun buildTextFrame(id: String, value: String): ByteArray {
        val text = value.toByteArray(Charsets.UTF_8)
        val content = byteArrayOf(3) + text
        val frame = ByteArray(10 + content.size)

        id.toByteArray(Charsets.US_ASCII).copyInto(frame)
        writeInt(frame, 4, content.size)
        content.copyInto(frame, 10)

        return frame
    }

    private fun writeInt(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value ushr 24).toByte()
        buffer[offset + 1] = (value ushr 16).toByte()
        buffer[offset + 2] = (value ushr 8).toByte()
        buffer[offset + 3] = value.toByte()
    }

    private fun writeSyncSafeInt(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = ((value ushr 21) and 0x7F).toByte()
        buffer[offset + 1] = ((value ushr 14) and 0x7F).toByte()
        buffer[offset + 2] = ((value ushr 7) and 0x7F).toByte()
        buffer[offset + 3] = (value and 0x7F).toByte()
    }

    private fun resolveLocalUri(track: Track): Uri? {
        return context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID),
            "${MediaStore.Audio.Media.TITLE} = ? AND ${MediaStore.Audio.Media.ARTIST} = ?",
            arrayOf(track.title, track.artist),
            null
        )?.use { cursor ->
            if (cursor.moveToFirst())
                ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                )
            else null
        }
    }

    private fun String.toUri(): Uri = Uri.parse(this)

    companion object {
        private const val MIME_TYPE = "audio/mpeg"
        private const val MY_MUSIC = "my_music"
        private const val BUFFER_SIZE = 8 * 1024

        private val AUDIO_COLUMNS = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )
    }
}