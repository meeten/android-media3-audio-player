package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.ui.extension.getSmallImageCacheKey

@Composable
fun LoadingTrackListImage(
    modifier: Modifier,
    smallImageUrl: String?,
    loadingProgressIndicatorSize: Dp? = null,
) {
    val context = LocalContext.current

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(smallImageUrl)
            .memoryCacheKey(smallImageUrl?.getSmallImageCacheKey())
            .build(),
        contentDescription = null,
        loading = {
            if (loadingProgressIndicatorSize != null)
                Loading(modifier = Modifier.size(loadingProgressIndicatorSize))
        },
        error = {
            Image(
                painter = painterResource(R.drawable.ic_track_placeholder),
                contentDescription = null
            )
        },
        modifier = modifier
    )
}

@Composable
fun LoadingPlayerImage(
    modifier: Modifier,
    trackListImageMemoryCacheKey: String?,
    largeImageUrl: String?
) {
    val context = LocalContext.current

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(largeImageUrl)
            .placeholderMemoryCacheKey(trackListImageMemoryCacheKey)
            .crossfade(true)
            .build(),
        contentDescription = null,
        error = {
            Image(
                painter = painterResource(R.drawable.ic_track_placeholder),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
        },
        modifier = modifier
    )
}