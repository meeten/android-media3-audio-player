package com.example.mediastoreexampleapp.presentation.tracks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediastoreexampleapp.domain.repository.TracksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val repository: TracksRepository
) : ViewModel() {

    fun getTracks() {
        viewModelScope.launch {
            val tracks = repository.getTracks()

            tracks.forEach {
                Log.d("TracksViewModel", it.toString())
            }
        }
    }
}