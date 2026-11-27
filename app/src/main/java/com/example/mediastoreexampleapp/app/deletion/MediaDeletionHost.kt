package com.example.mediastoreexampleapp.app.deletion

import android.Manifest
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.storage.MediaDeletionCoordinator
import com.example.domain.model.RemovingResult
import com.example.mediastoreexampleapp.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Lives above navigation, so switching screens cannot change the deletion target. */
@Composable
fun MediaDeletionHost(coordinator: MediaDeletionCoordinator) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pending by coordinator.confirmation.collectAsStateWithLifecycle()
    var launchedId by rememberSaveable { mutableStateOf<String?>(null) }
    var launchedUri by rememberSaveable { mutableStateOf<String?>(null) }
    var retryAfterApproval by rememberSaveable { mutableStateOf(false) }

    fun finish(approved: Boolean) {
        val id = launchedId
        val uri = launchedUri
        if (id != null && uri != null) {
            scope.launch {
                val outcome = coordinator.complete(
                    id, uri, retryAfterApproval, approved,
                )
                if (outcome == RemovingResult.Failed) {
                    Toast.makeText(
                        context,
                        com.example.ui.R.string.failed_to_delete_track,
                        Toast.LENGTH_LONG
                    ).show()
                }
                launchedId = null
                launchedUri = null
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.data?.hasExtra(ActivityResultContracts.StartIntentSenderForResult.EXTRA_SEND_INTENT_EXCEPTION) == true) {
            Toast.makeText(
                context,
                com.example.ui.R.string.failed_to_delete_track,
                Toast.LENGTH_LONG
            ).show()
        }
        finish(result.resultCode == Activity.RESULT_OK)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> finish(granted) }

    LaunchedEffect(coordinator) {
        coordinator.messages.collect { result ->
            val message = when (result) {
                RemovingResult.Busy -> R.string.track_deletion_busy
                RemovingResult.NotFound -> R.string.track_deletion_not_found
                else -> com.example.ui.R.string.failed_to_delete_track
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(pending?.id, launchedId) {
        val request = pending ?: return@LaunchedEffect
        if (launchedId != null) return@LaunchedEffect
        launchedId = request.id
        launchedUri = request.uri
        retryAfterApproval = request.retryAfterApproval
        try {
            val sender = request.intentSender
            if (sender == null) {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                launcher.launch(IntentSenderRequest.Builder(sender).build())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            coordinator.complete(
                request.id,
                request.uri,
                request.retryAfterApproval,
                approved = false
            )
            launchedId = null
            launchedUri = null
            Toast.makeText(context, com.example.ui.R.string.failed_to_delete_track, Toast.LENGTH_LONG).show()
        }
    }
}