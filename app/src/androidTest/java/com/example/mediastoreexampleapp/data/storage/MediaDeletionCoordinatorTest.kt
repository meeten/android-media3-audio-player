package com.example.mediastoreexampleapp.data.storage

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mediastoreexampleapp.domain.model.RemovingResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaDeletionCoordinatorTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun deletesOnlySelectedUriEvenWithIdenticalMetadata() = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        val first = createPendingAudio()
        val second = createPendingAudio()
        try {
            val coordinator = MediaDeletionCoordinator(context)
            assertEquals(RemovingResult.Deleted, coordinator.delete(second.toString()))
            assertTrue(exists(first))
            assertTrue(!exists(second))
            assertNull(coordinator.confirmation.value)
        } finally {
            context.contentResolver.delete(first, null, null)
            if (exists(second)) context.contentResolver.delete(second, null, null)
        }
    }

    @Test fun rejectsNetworkAndCollectionUris() = runBlocking {
        val coordinator = MediaDeletionCoordinator(context)
        assertEquals(RemovingResult.Failed, coordinator.delete("https://example.com/preview.mp3"))
        assertEquals(RemovingResult.Failed, coordinator.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()))
        assertEquals(RemovingResult.Failed, coordinator.delete("content://other.provider/external/audio/media/43"))
    }

    @Test fun rejectedConfirmationDoesNotDeleteTheTarget() = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        val uri = createPendingAudio()
        try {
            val coordinator = MediaDeletionCoordinator(context)
            assertNull(coordinator.complete("restored-request", uri.toString(), true, false))
            assertTrue(exists(uri))
        } finally {
            context.contentResolver.delete(uri, null, null)
        }
    }

    @Test fun approvedAndroid10ContinuationRetriesSavedUri() = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        val uri = createPendingAudio()
        try {
            val coordinator = MediaDeletionCoordinator(context)
            assertEquals(RemovingResult.Deleted, coordinator.complete("restored-request", uri.toString(), true, true))
            assertTrue(!exists(uri))
        } finally {
            if (exists(uri)) context.contentResolver.delete(uri, null, null)
        }
    }

    private fun createPendingAudio(): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "deletion-test-${System.nanoTime()}.mp3")
            put(MediaStore.Audio.Media.TITLE, "Identical test title")
            put(MediaStore.Audio.Media.ARTIST, "Identical test artist")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/MediaStoreExampleAppTests")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        return requireNotNull(context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values))
    }

    private fun exists(uri: Uri): Boolean = context.contentResolver.query(
        uri, arrayOf(MediaStore.Audio.Media._ID), null, null, null,
    )?.use { it.moveToFirst() } == true
}