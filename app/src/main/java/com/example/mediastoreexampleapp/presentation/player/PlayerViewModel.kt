package com.example.mediastoreexampleapp.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediastoreexampleapp.core.extension.toTrackKey
import com.example.mediastoreexampleapp.domain.model.RemovingResult
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.model.TrackAvailabilityResult
import com.example.mediastoreexampleapp.domain.model.TrackOperation
import com.example.mediastoreexampleapp.domain.model.withLocalAvailability
import com.example.mediastoreexampleapp.domain.playback.PlayerController
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import com.example.mediastoreexampleapp.domain.usecase.ToggleTrackAvailabilityUseCase
import com.example.mediastoreexampleapp.presentation.player.model.PlayerUiEvent
import com.example.mediastoreexampleapp.presentation.player.model.PlayerScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController,
    localAudioRepository: LocalAudioRepository,
    private val downloadUseCase: ToggleTrackAvailabilityUseCase
) : ViewModel() {

    val uiState =
        combine(
            playerController.playerState,
            localAudioRepository.tracksFlow,
            localAudioRepository.operationsFlow,
        ) { playerState, tracks, operations ->
            val playerState = playerState.copy(
                currentTrack = playerState.currentTrack?.let { current ->
                    if (tracks == null) current else current.withLocalAvailability(tracks)
                }
            )
            val downloading = operations[playerState.currentTrack?.toTrackKey()]
            PlayerScreenState(playerState, downloading == TrackOperation.DOWNLOADING)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerScreenState(playerController.playerState.value)
        )

    private val _uiEvent = Channel<PlayerUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var downloadJob: Job? = null
    fun download(track: Track) {
        if (downloadJob?.isActive == true) return
        downloadJob = viewModelScope.launch {
            when (val trackAvailabilityResult = downloadUseCase(track)) {
                is TrackAvailabilityResult.Download -> {
                    if (!trackAvailabilityResult.isSuccess) _uiEvent.trySend(PlayerUiEvent.DownloadingFailed)
                }

                is TrackAvailabilityResult.Delete -> {
                    when (trackAvailabilityResult.removingResult) {
                        RemovingResult.Failed -> _uiEvent.trySend(PlayerUiEvent.RemovingFailed)

                        else -> {}
                    }
                }
            }
        }
    }

    fun play() {
        viewModelScope.launch {
            playerController.play()
        }
    }

    fun pause() {
        viewModelScope.launch {
            playerController.pause()
        }
    }

    fun changeCurrentPosition(currentPositionMs: Long) {
        viewModelScope.launch {
            playerController.setCurrentPosition(currentPositionMs)
        }
    }

    fun next() {
        viewModelScope.launch {
            playerController.next()
        }
    }

    fun previous() {
        viewModelScope.launch {
            playerController.previous()
        }
    }
}