package com.example.mediastoreexampleapp.data.repository

import android.content.Context
import android.database.ContentObserver
import android.provider.MediaStore
import com.example.mediastoreexampleapp.data.mapper.toTrack
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
        MediaStore.Audio.Media.DURATION
    )

    override val tracksFlow: StateFlow<List<Track>> = callbackFlow {
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
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

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
}