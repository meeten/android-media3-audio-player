package com.example.mediastoreexampleapp.presentation.tracks

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediastoreexampleapp.presentation.tracks.model.TracksScreenState
import com.example.mediastoreexampleapp.presentation.tracks.ui.TracksScreenContent

@Composable
fun TracksScreen(
    modifier: Modifier = Modifier,
    onTrackClick: () -> Unit,
    viewModel: TracksViewModel = hiltViewModel()
) {

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    RequestAudioPermission(viewModel::getTracks)

    when (val currentState = uiState.value) {
        TracksScreenState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is TracksScreenState.Content -> {
            TracksScreenContent(
                modifier = modifier,
                tracks = currentState.tracks,
                onTrackClick = { track ->
                    viewModel.setPlaylistWithCurrentTrack(currentState.tracks, track)
                    onTrackClick()
                }
            )
        }

        is TracksScreenState.Failure -> {
            Text(text = "Error", fontSize = 36.sp)
        }
    }
}

@Composable
fun RequestAudioPermission(
    onGranted: () -> Unit
) {
    val context = LocalContext.current

    val permission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onGranted()
    }

    LaunchedEffect(Unit) {
        val isGranted =
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        if (isGranted) {
            onGranted()
        } else {
            launcher.launch(permission)
        }
    }
}