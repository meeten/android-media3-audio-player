package com.example.mediastoreexampleapp.presentation.remote_tracks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediastoreexampleapp.domain.repository.RemoteAudioRepository
import com.example.mediastoreexampleapp.presentation.remote_tracks.model.RemoteTracksScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class RemoteTracksViewModel @Inject constructor(
    private val remoteAudioRepository: RemoteAudioRepository,
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

    fun search(query: String) {
        if (query.isEmpty()) {
            getTracks()
            return
        }

        viewModelScope.launch {
            delay(600.milliseconds)

            try {
                val tracks = remoteAudioRepository.search(query)
                _uiState.value = RemoteTracksScreenState.Content(tracks)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = RemoteTracksScreenState.Failure(message = e.message)
            }
        }
    }

    fun retry() {
        getTracks()
    }
}