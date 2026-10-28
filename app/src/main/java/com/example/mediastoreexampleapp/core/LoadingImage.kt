package com.example.mediastoreexampleapp.core

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil3.compose.SubcomposeAsyncImage
import com.example.mediastoreexampleapp.R

@Composable
fun LoadingImage(
    modifier: Modifier,
    loadingProgressIndicatorSize: Dp,
    imageUrl: String?
) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = null,
        loading = { Loading(modifier = Modifier.size(loadingProgressIndicatorSize)) },
        error = {
            Image(
                painter = painterResource(R.drawable.ic_track_placeholder),
                contentDescription = null
            )
        },
        modifier = modifier
    )
}