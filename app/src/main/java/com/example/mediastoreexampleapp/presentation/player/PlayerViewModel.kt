package com.example.mediastoreexampleapp.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.playback.PlayerController
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import com.example.mediastoreexampleapp.domain.usecase.DownloadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController,
    localAudioRepository: LocalAudioRepository,
    private val downloadUseCase: DownloadUseCase
) : ViewModel() {

    val uiState =
        combine(
            playerController.playerState,
            localAudioRepository.tracksFlow
        ) { playerState, tracks ->
            playerState.copy(
                currentTrack = playerState.currentTrack?.copy(
                    isDownload = tracks?.any { track ->
                        playerState.currentTrack.artist == track.artist
                                && playerState.currentTrack.title == track.title
                    } ?: playerState.currentTrack.isDownload
                )
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = playerController.playerState.value
        )


    private var downloadJob: Job? = null
    fun download(track: Track) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            downloadUseCase(track)
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