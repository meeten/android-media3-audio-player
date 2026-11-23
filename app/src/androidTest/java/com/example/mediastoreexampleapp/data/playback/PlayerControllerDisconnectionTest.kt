package com.example.mediastoreexampleapp.data.playback

import android.content.ComponentName
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mediastoreexampleapp.data.service.PlaybackService
import com.example.mediastoreexampleapp.domain.model.Track
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PlayerControllerDisconnectionTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun releasedControllerDoesNotReconnectWhenPlaylistIsSet() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val originalFuture = connect()
        var observerFuture: ListenableFuture<MediaController>? = null
        try {
            val original = originalFuture.get(15, TimeUnit.SECONDS)
            val playerController = PlayerControllerImpl(originalFuture, scope)
            withContext(Dispatchers.Main) {
                assertTrue(original.isConnected)
                // Ensure the implementation has consumed its single controller before release.
                playerController.pause()
                MediaController.releaseFuture(originalFuture)
                assertFalse(original.isConnected)
            }

            val track = Track(
                id = 42,
                title = "Test track",
                artist = "Test artist",
                durationMs = 1000,
                smallImageUrl = null,
                largetImageUrl = null,
                trackUri = "file:///nonexistent-test-track.mp3",
                isPreview = false,
                isDownload = false,
            )
            withContext(Dispatchers.Main) {
                playerController.setPlaylist(listOf(track), 0)
                assertEquals(track, playerController.playerState.value.currentTrack)
                assertFalse(original.isConnected)
            }

            val newFuture = connect()
            observerFuture = newFuture
            val observer = newFuture.get(15, TimeUnit.SECONDS)
            withContext(Dispatchers.Main) {
                assertTrue(observer.isConnected)
                assertNull(observer.currentMediaItem)
            }
        } finally {
            withContext(Dispatchers.Main) {
                observerFuture?.let(MediaController::releaseFuture)
                MediaController.releaseFuture(originalFuture)
                scope.cancel()
            }
        }
    }

    private fun connect(): ListenableFuture<MediaController> {
        lateinit var future: ListenableFuture<MediaController>
        instrumentation.runOnMainSync {
            val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            future = MediaController.Builder(context, token).buildAsync()
        }
        return future
    }
}