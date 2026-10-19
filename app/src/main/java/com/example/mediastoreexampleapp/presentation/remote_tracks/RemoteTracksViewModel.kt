package com.example.mediastoreexampleapp.presentation.remote_tracks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.repository.PlayerController
import com.example.mediastoreexampleapp.domain.repository.RemoteAudioRepository
import com.example.mediastoreexampleapp.presentation.remote_tracks.model.RemoteTracksScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoteTracksViewModel @Inject constructor(
    private val remoteAudioRepository: RemoteAudioRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<RemoteTracksScreenState>(RemoteTracksScreenState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        getTracks()
    }

    private fun getTracks() {
        viewModelScope.launch {
            _uiState.value = RemoteTracksScreenState.Loading

            try {
                val tracks = remoteAudioRepository.getTracks()
                _uiState.value = RemoteTracksScreenState.Content(tracks)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = RemoteTracksScreenState.Failure(message = e.message)
            }
        }
    }

    fun setPlaylistWithCurrentTrack(tracks: List<Track>, track: Track) {
        viewModelScope.launch {
            val currentIndex = tracks.indexOfFirst { track.id == it.id }
            if (currentIndex !in tracks.indices) return@launch
            playerController.setPlaylist(playlist = tracks, startIndex = currentIndex)
        }
    }

    fun retry() {
        getTracks()
    }
}