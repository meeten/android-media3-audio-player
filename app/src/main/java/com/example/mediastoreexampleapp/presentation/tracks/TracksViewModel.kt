package com.example.mediastoreexampleapp.presentation.tracks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val repository: LocalAudioRepository
) : ViewModel() {

    fun getTracks() {
        viewModelScope.launch {
            repository.tracksFlow.collect { tracks ->
                tracks.forEach {
                    Log.d("TracksViewModel", it.toString())
                }
            }
        }
    }
}