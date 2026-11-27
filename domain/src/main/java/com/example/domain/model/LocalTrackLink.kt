package com.example.domain.model

/** Never infer a deletable file from title/artist: they are not identifiers. */
fun Track.withLocalAvailability(localTracks: List<Track>): Track {
    val localCopy = if (isPreview) {
        localTracks.singleOrNull { it.sourceRemoteId == id }
    } else {
        localTracks.singleOrNull { it.trackUri == trackUri }
    }
    return copy(
        isDownload = localCopy != null,
        localCopyUri = localCopy?.trackUri,
    )
}