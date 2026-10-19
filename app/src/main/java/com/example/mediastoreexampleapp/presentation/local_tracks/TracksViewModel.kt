package com.example.mediastoreexampleapp.presentation.local_tracks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import com.example.mediastoreexampleapp.presentation.local_tracks.model.TracksScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val repository: LocalAudioRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TracksScreenState>(TracksScreenState.PermissionRequired)
    val uiState = _uiState.asStateFlow()

    var tracksJob: Job? = null
    fun getTracks() {
        if (tracksJob?.isActive == true) return

        tracksJob = viewModelScope.launch {
            _uiState.value = TracksScreenState.Loading
            repository.tracksFlow.collect { tracks ->
                _uiState.value = TracksScreenState.Content(tracks)
            }
        }
    }
}