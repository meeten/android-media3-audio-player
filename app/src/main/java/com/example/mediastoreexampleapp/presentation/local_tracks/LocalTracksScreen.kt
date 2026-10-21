package com.example.mediastoreexampleapp.presentation.local_tracks

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediastoreexampleapp.R
import com.example.mediastoreexampleapp.core.Loading
import com.example.mediastoreexampleapp.core.SearchBarCustom
import com.example.mediastoreexampleapp.core.TracksScreenContent
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.presentation.local_tracks.model.TracksScreenState

@Composable
fun LocalTracksScreen(
    modifier: Modifier = Modifier,
    onTrackClick: (List<Track>, Track) -> Unit,
    viewModel: LocalTracksViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    when (val currentState = uiState.value) {
        TracksScreenState.PermissionRequired -> {
            RequestAudioPermission(viewModel::getTracks)
        }

        TracksScreenState.Loading -> {
            Loading()
        }

        is TracksScreenState.Content -> {
            Column(modifier) {
                SearchBarCustom(
                    modifier = Modifier.padding(8.dp),
                    onSearch = viewModel::search
                )

                TracksScreenContent(
                    tracks = currentState.tracks,
                    onTrackClick = { track ->
                        onTrackClick(currentState.tracks, track)
                    }
                )
            }
        }
    }
}

@Composable
fun RequestAudioPermission(
    onGranted: () -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    var isGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasRequestedOnce by remember { mutableStateOf(false) }
    var canAskAgain by remember { mutableStateOf(false) }

    val currentOnGranted by rememberUpdatedState(onGranted)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isGranted = granted
        hasRequestedOnce = true
        canAskAgain = !granted && activity?.shouldShowRequestPermissionRationale(permission) == true
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isGranted = ContextCompat.checkSelfPermission(context, permission) ==
                        PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(isGranted) {
        if (isGranted) currentOnGranted()
    }

    LaunchedEffect(Unit) {
        if (!isGranted) {
            launcher.launch(permission)
        }
    }

    if (!isGranted && hasRequestedOnce) {
        PermissionDenied(
            canAskAgain = canAskAgain,
            onAction = {
                if (canAskAgain) {
                    launcher.launch(permission)
                } else {
                    activity?.openAppSettings()
                }
            }
        )
    }
}

@Composable
private fun PermissionDenied(
    canAskAgain: Boolean,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.audio_permission_required))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.audio_permission_description))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(
                    if (canAskAgain) R.string.grant_audio_permission
                    else R.string.open_settings
                )
            )
        }
    }
}

private fun Activity.openAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
    )
}