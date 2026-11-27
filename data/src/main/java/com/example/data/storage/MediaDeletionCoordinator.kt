package com.example.data.storage

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.domain.model.RemovingResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaDeletionCoordinator @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    data class Confirmation(
        val id: String,
        val uri: String,
        val intentSender: IntentSender?,
        val retryAfterApproval: Boolean,
    )

    private val mutex = Mutex()
    private val _confirmation = MutableStateFlow<Confirmation?>(null)
    val confirmation = _confirmation.asStateFlow()
    private val _messages = Channel<RemovingResult>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    suspend fun delete(uriString: String): RemovingResult {
        val result = deleteInternal(uriString)
        if (result == RemovingResult.Failed || result == RemovingResult.NotFound ||
            result == RemovingResult.Busy
        ) _messages.trySend(result)
        return result
    }

    private suspend fun deleteInternal(uriString: String): RemovingResult =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                if (_confirmation.value != null) return@withLock RemovingResult.Busy
                val uri = uriString.toUri()
                if (!isAudioItem(uri)) return@withLock RemovingResult.Failed
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    val request = Confirmation(UUID.randomUUID().toString(), uriString, null, true)
                    _confirmation.value = request
                    return@withLock RemovingResult.NeedsConfirmation(request.id)
                }
                try {
                    deleteDirectly(uri)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: SecurityException) {
                    try {
                        val retry: Boolean
                        val sender = when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                                retry = false
                                MediaStore.createDeleteRequest(context.contentResolver, listOf(uri))
                                    .intentSender
                            }

                            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q &&
                                    e is RecoverableSecurityException -> {
                                retry = true
                                e.userAction.actionIntent.intentSender
                            }

                            else -> return@withLock RemovingResult.Failed
                        }
                        val request =
                            Confirmation(UUID.randomUUID().toString(), uriString, sender, retry)
                        _confirmation.value = request
                        RemovingResult.NeedsConfirmation(request.id)
                    } catch (requestError: Exception) {
                        Log.e("MediaDeletion", "Cannot prepare confirmation", requestError)
                        RemovingResult.Failed
                    }
                } catch (e: Exception) {
                    Log.e("MediaDeletion", "Cannot delete $uri", e)
                    RemovingResult.Failed
                }
            }
        }

    suspend fun complete(
        requestId: String,
        uriString: String,
        retryAfterApproval: Boolean,
        approved: Boolean,
    ): RemovingResult? = withContext(Dispatchers.IO) {
        mutex.withLock {
            val pending = _confirmation.value
            if (pending != null && pending.id != requestId) return@withLock RemovingResult.Busy
            try {
                when {
                    !approved -> null
                    !retryAfterApproval -> RemovingResult.Deleted
                    !isAudioItem(uriString.toUri()) -> RemovingResult.Failed
                    else -> deleteDirectly(uriString.toUri())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MediaDeletion", "Deletion after confirmation failed", e)
                RemovingResult.Failed
            } finally {
                _confirmation.value = null
            }
        }
    }

    private fun deleteDirectly(uri: Uri): RemovingResult =
        if (context.contentResolver.delete(uri, null, null) > 0) RemovingResult.Deleted
        else RemovingResult.NotFound

    private fun isAudioItem(uri: Uri): Boolean =
        uri.scheme == "content" && uri.authority == MediaStore.AUTHORITY &&
                uri.pathSegments.size == 4 && uri.pathSegments[1] == "audio" &&
                uri.pathSegments[2] == "media" &&
                runCatching { ContentUris.parseId(uri) > 0 }.getOrDefault(false)
}