package com.example.remote_tracks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.RemovingResult
import com.example.domain.model.Track
import com.example.domain.model.TrackAvailabilityResult
import com.example.domain.model.withLocalAvailability
import com.example.domain.repository.LocalAudioRepository
import com.example.domain.repository.RemoteAudioRepository
import com.example.domain.usecase.ToggleTrackAvailabilityUseCase
import com.example.remote_tracks.model.RemoteTracksEvent
import com.example.remote_tracks.model.RemoteTracksScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class RemoteTracksViewModel @Inject constructor(
    private val remoteAudioRepository: RemoteAudioRepository,
    localAudioRepository: LocalAudioRepository,
    private val downloadUseCase: ToggleTrackAvailabilityUseCase
) : ViewModel() {

    private val _queryFlow = MutableStateFlow("")
    val queryFlow = _queryFlow.asStateFlow()

    private val _uiEvent = Channel<RemoteTracksEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val downloadedTracksFlow = localAudioRepository.tracksFlow
    private val operationsFlow = localAudioRepository.operationsFlow
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
            when (val downloadResult = downloadUseCase(track)) {
                is TrackAvailabilityResult.Download -> {
                    if (!downloadResult.isSuccess) {
                        _uiEvent.trySend(RemoteTracksEvent.DownloadFailed)
                    }
                }

                is TrackAvailabilityResult.Delete -> {
                    when (downloadResult.removingResult) {
                        is RemovingResult.Failed -> _uiEvent.trySend(RemoteTracksEvent.DeleteFailed)
                        else -> {}
                    }
                }
            }
        }
    }

    private fun loadTracks(query: String): Flow<RemoteTracksScreenState> = combine(
        getTracks(query),
        operationsFlow,
        downloadedTracksFlow
    ) { state, operations, downloadedTracks ->
        if (state is RemoteTracksScreenState.Content) {
            val downloads = downloadedTracks ?: emptyList()
            val tracks =
                state.tracks.map { track ->
                    val trackKey = track.toTrackKey()
                    val currentOperation = operations[trackKey]
                    track.withLocalAvailability(downloads).copy(
                        isDownloading = currentOperation != null,
                    )
                }
            RemoteTracksScreenState.Content(tracks = tracks)
        } else {
            state
        }
    }

    private fun getTracks(query: String) = flow {
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
    }
}