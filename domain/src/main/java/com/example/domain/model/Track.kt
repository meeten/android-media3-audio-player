package com.example.domain.model

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val smallImageUrl: String?,
    val largetImageUrl: String?,
    val trackUri: String,
    val isPreview: Boolean,
    val isDownload: Boolean,
    val isDownloading: Boolean = false,
    val sourceRemoteId: Long? = null,
    val localCopyUri: String? = null,
) {
    fun toTrackKey(): TrackKey {
        return TrackKey(
            id = id
        )
    }
}