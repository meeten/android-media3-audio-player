package com.example.mediastoreexampleapp.core.extension

import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.model.TrackKey

fun Track.toTrackKey(): TrackKey {
    return TrackKey(
        id = id
    )
}