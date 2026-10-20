package com.example.mediastoreexampleapp.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediastoreexampleapp.domain.playback.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController
) : ViewModel() {

    val uiState = playerController.playerState

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