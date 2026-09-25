package com.example.mediastoreexampleapp.presentation.player

import androidx.lifecycle.ViewModel
import com.example.mediastoreexampleapp.presentation.player.model.PlayerScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    //TODO позже добавится конструктор с currentIndex и playlist
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerScreenState())
    val uiState = _uiState.asStateFlow()

    fun changePlaying(isPlaying: Boolean) {
        _uiState.update { it.copy(isPlaying = isPlaying) }
    }
}