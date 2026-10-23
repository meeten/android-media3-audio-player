package com.example.mediastoreexampleapp.presentation.local_tracks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediastoreexampleapp.domain.usecase.SearchLocalAudioUseCase
import com.example.mediastoreexampleapp.presentation.local_tracks.model.LocalTracksScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class LocalTracksViewModel @Inject constructor(
    private val searchLocalAudioUseCase: SearchLocalAudioUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LocalTracksScreenState>(LocalTracksScreenState.PermissionRequired)
    val uiState = _uiState.asStateFlow()

    private val _queryFlow = MutableStateFlow("")
    val queryFlow = _queryFlow.asStateFlow()

    private var tracksJob: Job? = null
    fun getTracks() {
        if (tracksJob?.isActive == true) return

        tracksJob = viewModelScope.launch {
            _queryFlow.map { it.trim() }
                .distinctUntilChanged()
                .debounce { query ->
                    if (query.isEmpty()) Duration.ZERO else 600.milliseconds
                }
                .flatMapLatest { query ->
                    searchLocalAudioUseCase(query)
                }.collect { tracks ->
                    _uiState.value =
                        tracks?.let {
                            LocalTracksScreenState.Content(tracks = tracks)
                        } ?: LocalTracksScreenState.Loading
                }
        }
    }

    fun onQueryChange(value: String) {
        _queryFlow.value = value
    }
}