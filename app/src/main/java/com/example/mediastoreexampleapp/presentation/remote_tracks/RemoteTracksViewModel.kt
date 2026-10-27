package com.example.mediastoreexampleapp.presentation.remote_tracks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import com.example.mediastoreexampleapp.domain.repository.RemoteAudioRepository
import com.example.mediastoreexampleapp.domain.usecase.DownloadUseCase
import com.example.mediastoreexampleapp.presentation.remote_tracks.model.RemoteTracksScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class RemoteTracksViewModel @Inject constructor(
    private val remoteAudioRepository: RemoteAudioRepository,
    localAudioRepository: LocalAudioRepository,
    private val downloadUseCase: DownloadUseCase

) : ViewModel() {
    private val _queryFlow = MutableStateFlow("")
    val queryFlow = _queryFlow.asStateFlow()

    private val downloadedTracksFlow = localAudioRepository.tracksFlow
    private val retryTrigger = MutableStateFlow(0)

    private val debounceQuery = _queryFlow
        .map { it.trim() }
        .distinctUntilChanged()
        .debounce(600.milliseconds)

    val uiState = combine(debounceQuery, retryTrigger) { query, _ ->
        query
    }.flatMapLatest { query ->
        loadTracks(query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RemoteTracksScreenState.Loading
    )

    fun onChangeQuery(query: String) {
        _queryFlow.value = query
    }

    fun retry() {
        retryTrigger.value += 1
    }

    fun download(track: Track) {
        viewModelScope.launch {
            downloadUseCase(track)
        }
    }

    private fun loadTracks(query: String): Flow<RemoteTracksScreenState> = combine(flow {
        emit(
            try {
                val tracks =
                    if (query.isEmpty()) remoteAudioRepository.getTracks()
                    else remoteAudioRepository.search(query)
                RemoteTracksScreenState.Content(tracks = tracks)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                RemoteTracksScreenState.Failure(message = e.message)
            }
        )
    }, downloadedTracksFlow) { state, downloaded ->
        val downloaded = downloaded ?: emptyList()
        if (state is RemoteTracksScreenState.Content) {
            val tracks =
                state.tracks.map { track ->
                    track.copy(
                        isDownload = downloaded.any {
                            it.artist == track.artist && it.title == track.title
                        })
                }
            RemoteTracksScreenState.Content(tracks = tracks)
        } else {
            state
        }
    }
}