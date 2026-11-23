package com.example.mediastoreexampleapp.data.service

import android.content.ComponentName
import android.os.Process
import android.os.SystemClock
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.util.concurrent.ListenableFuture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PlaybackServiceLifecycleTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun reconnectsAfterServiceIsRemoved() {
        val pid = Process.myPid()
        assertTrue("PlaybackService must not be running before the test", !isServiceRunning())
        var future: ListenableFuture<MediaController>? = null

        try {
            val firstFuture = connect()
            future = firstFuture
            val firstController = firstFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            waitUntil("PlaybackService did not start") { isServiceRunning() }
            onMain {
                assertTrue(firstController.isConnected)
                firstController.setMediaItem(MediaItem.Builder().setMediaId("first-session").build())
            }
            waitUntil("first controller did not receive the media item") {
                var mediaId: String? = null
                onMain { mediaId = firstController.currentMediaItem?.mediaId }
                mediaId == "first-session"
            }

            onMain { MediaController.releaseFuture(firstFuture) }
            future = null
            waitUntil("PlaybackService was not destroyed; another client may still be bound") {
                !isServiceRunning()
            }

            future = connect()
            val secondController = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            waitUntil("PlaybackService did not restart") { isServiceRunning() }
            assertEquals(pid, Process.myPid())

            onMain {
                assertTrue(secondController.isConnected)
                assertNull(secondController.currentMediaItem)
                secondController.setMediaItem(MediaItem.Builder().setMediaId("lifecycle-test").build())
            }
            waitUntil("new controller did not receive the media item") {
                var mediaId: String? = null
                onMain { mediaId = secondController.currentMediaItem?.mediaId }
                mediaId == "lifecycle-test"
            }
        } finally {
            future?.let { controllerFuture -> onMain { MediaController.releaseFuture(controllerFuture) } }
        }
    }

    private fun connect(): ListenableFuture<MediaController> {
        lateinit var future: ListenableFuture<MediaController>
        onMain {
            val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            future = MediaController.Builder(context, token).buildAsync()
        }
        return future
    }

    private fun isServiceRunning(): Boolean {
        val descriptor = instrumentation.uiAutomation.executeShellCommand(
            "dumpsys activity services ${context.packageName}/${PlaybackService::class.java.name}"
        )
        val text = descriptor.use { FileInputStream(it.fileDescriptor).bufferedReader().readText() }
        check(text.contains("ACTIVITY MANAGER SERVICES")) { "Unexpected dumpsys output: $text" }
        return text.contains("ServiceRecord{") && text.contains(PlaybackService::class.java.simpleName)
    }

    private fun waitUntil(message: String, condition: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS)
        while (SystemClock.elapsedRealtime() < deadline) {
            if (condition()) return
            SystemClock.sleep(100)
        }
        throw AssertionError(message)
    }

    private fun onMain(block: () -> Unit) = instrumentation.runOnMainSync(block)

    private companion object {
        const val TIMEOUT_SECONDS = 15L
    }
}