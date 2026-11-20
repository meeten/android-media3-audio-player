package com.example.mediastoreexampleapp.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.playback.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val playerController: PlayerController
) : ViewModel() {

    val playerState = playerController.playerState

    fun setPlaylistWithCurrentTrack(tracks: List<Track>, track: Track) {
        viewModelScope.launch {
            val currentIndex = tracks.indexOfFirst { track.id == it.id }
            if (currentIndex !in tracks.indices) return@launch
            playerController.setPlaylist(playlist = tracks, startIndex = currentIndex)
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

    fun changeCurrentPosition(currentPositionMs: Long) {
        viewModelScope.launch {
            playerController.setCurrentPosition(currentPositionMs)
        }
    }
}