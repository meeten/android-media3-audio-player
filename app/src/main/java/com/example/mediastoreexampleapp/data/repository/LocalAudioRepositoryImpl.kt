package com.example.mediastoreexampleapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.mediastoreexampleapp.core.extension.toTrackKey
import com.example.mediastoreexampleapp.data.audio.ArtworkDownloader
import com.example.mediastoreexampleapp.data.audio.Mp3MetadataWriter
import com.example.mediastoreexampleapp.data.mapper.toTrack
import com.example.mediastoreexampleapp.data.storage.MediaDeletionCoordinator
import com.example.mediastoreexampleapp.domain.model.RemovingResult
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.model.TrackKey
import com.example.mediastoreexampleapp.domain.model.TrackOperation
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class LocalAudioRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    coroutineScope: CoroutineScope,
    private val artworkDownloader: ArtworkDownloader,
    private val mediaDeletionCoordinator: MediaDeletionCoordinator,
) : LocalAudioRepository {

    private val _operationsFlow = MutableStateFlow<Map<TrackKey, TrackOperation>>(emptyMap())
    override val operationsFlow: StateFlow<Map<TrackKey, TrackOperation>> =
        _operationsFlow.asStateFlow()

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

    private val insertMutex = Mutex()
    override suspend fun insert(track: Track): Boolean =
        withContext(Dispatchers.IO) {
            val trackKey = track.toTrackKey()

            insertMutex.withLock {
                val operations = _operationsFlow.value

                if (operations[trackKey] != null) return@withContext false
                _operationsFlow.update { currentOperations ->
                    currentOperations + mapOf(trackKey to TrackOperation.DOWNLOADING)
                }
            }

            var uri: Uri? = null
            try {
                uri = createAudioUri(track) ?: return@withContext false
                downloadAudio(track, uri)
                publishAudio(uri)
                true
            } catch (e: CancellationException) {
                uri?.let { deleteAudio(it) }
                throw e
            } catch (e: Exception) {
                Log.e("LocalAudioRepository", "Failed to download ${track.title}", e)
                uri?.let { deleteAudio(it) }
                false
            } finally {
                _operationsFlow.update { currentOperations ->
                    currentOperations - trackKey
                }
            }
        }

    override suspend fun remove(track: Track): RemovingResult {
        val uri = track.localCopyUri ?: if (!track.isPreview) track.trackUri else null
        return if (uri == null) RemovingResult.NotFound else mediaDeletionCoordinator.delete(uri)
    }

    private fun createAudioUri(track: Track): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "${track.title}.mp3")
            put(MediaStore.Audio.Media.TITLE, track.title)
            put(MediaStore.Audio.Media.ARTIST, track.artist)
            put(MediaStore.Audio.Media.ALBUM, albumName(track))
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

    private suspend fun downloadAudio(track: Track, uri: Uri) {
        val coroutineContext = currentCoroutineContext()
        val artwork = try {
            artworkDownloader.download(track.smallImageUrl)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Log.w("LocalAudioRepository", "Saving ${track.title} without artwork", e)
            null
        }
        coroutineContext.ensureActive()
        val connection = URL(track.trackUri).openConnection().apply {
            connectTimeout = 15_000
            readTimeout = 15_000
        }
        connection.getInputStream().use { input ->
            context.contentResolver.openOutputStream(uri, "w")?.use { output ->
                Mp3MetadataWriter.write(
                    input, output, track.title, track.artist, albumName(track), artwork,
                    checkCancellation = { coroutineContext.ensureActive() }
                )
            } ?: throw IOException("Cannot open output stream")
        }
        coroutineContext.ensureActive()
    }

    private fun publishAudio(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.IS_PENDING, 0)
            }

            if (context.contentResolver.update(uri, values, null, null) == 0) {
                throw IOException("Cannot publish audio: $uri")
            }
        } else {
            @Suppress("DEPRECATION")
            val path = context.contentResolver.query(
                uri, arrayOf(MediaStore.Audio.Media.DATA), null, null, null
            )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
            if (path != null) {
                MediaScannerConnection.scanFile(context, arrayOf(path), arrayOf(MIME_TYPE), null)
            }
        }
    }

    private fun deleteAudio(uri: Uri) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            Log.e("LocalAudioRepository", "Failed to clean up $uri", e)
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

    private fun albumName(track: Track): String = "MediaStoreExampleApp track ${track.id}"

    companion object {
        private const val MIME_TYPE = "audio/mpeg"
        private const val MY_MUSIC = "my_music"

        private val AUDIO_COLUMNS = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
        )
    }
}