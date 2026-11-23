package com.example.mediastoreexampleapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LocalTrackLinkTest {
    private fun track(id: Long, preview: Boolean, sourceId: Long? = null) = Track(
        id = id,
        title = "Same title",
        artist = "Same artist",
        durationMs = 30_000,
        trackUri = if (preview) "https://example.com/$id.mp3" else "content://media/external/audio/media/$id",
        smallImageUrl = null,
        isPreview = preview,
        isDownload = !preview,
        sourceRemoteId = sourceId,
    )

    @Test fun matchingMetadataDoesNotSelectUnrelatedFile() {
        val result = track(1, true).withLocalAvailability(listOf(track(43, false)))
        assertFalse(result.isDownload)
        assertNull(result.localCopyUri)
    }

    @Test fun remoteTrackLinksOnlyItsRecordedLocalCopy() {
        val local = track(43, false, sourceId = 1)
        val result = track(1, true).withLocalAvailability(listOf(track(41, false), local))
        assertEquals(local.trackUri, result.localCopyUri)
        assertEquals("https://example.com/1.mp3", result.trackUri)
    }

    @Test fun ambiguousCopiesAreNeverChosenArbitrarily() {
        val result = track(1, true).withLocalAvailability(
            listOf(track(41, false, 1), track(43, false, 1)),
        )
        assertNull(result.localCopyUri)
        assertFalse(result.isDownload)
    }

    @Test fun localTrackUsesExactUriEvenWhenMetadataMatches() {
        val selected = track(43, false)
        val result = selected.withLocalAvailability(listOf(track(41, false), selected))
        assertEquals(selected.trackUri, result.localCopyUri)
    }

    @Test fun removedFileClearsStaleDeletionTarget() {
        val result = track(1, true).copy(
            isDownload = true, localCopyUri = "content://media/external/audio/media/43",
        ).withLocalAvailability(emptyList())
        assertFalse(result.isDownload)
        assertNull(result.localCopyUri)
    }
}