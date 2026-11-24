package com.example.mediastoreexampleapp.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mediastoreexampleapp.app.navigation.NavigationItem
import com.example.mediastoreexampleapp.app.navigation.NavigationState
import com.example.mediastoreexampleapp.domain.model.PlayerState

private val navigationItems = listOf(
    NavigationItem.RemoteTracks,
    NavigationItem.LocalTracks,
)

@Composable
fun BottomAppBarCustom(
    navigationState: NavigationState,
    playerState: PlayerState,
    modifier: Modifier = Modifier,
    onPrevious: () -> Unit,
    onPause: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit,
    onClickMiniAudioPlayer: () -> Unit,
    onChangeCurrentPosition: (Long) -> Unit,
) {
    val navBackStackEntry = navigationState.navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
    ) {
        MiniAudiPlayer(
            playerState = playerState,
            route = currentDestination?.route,
            playbackDurationMs = playerState.durationMs,
            currentPositionMs = playerState.currentPositionMs,
            onPrevious = onPrevious,
            onPause = onPause,
            onPlay = onPlay,
            onNext = onNext,
            onClickMiniAudioPlayer = onClickMiniAudioPlayer,
            onChangeCurrentPosition = onChangeCurrentPosition
        )

        BottomAppBar(
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            navigationItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any {
                    it.route == item.screen.route
                } == true
                NavigationBarItem(
                    selected = selected,
                    onClick = { navigationState.navigateTo(item.screen.route) },
                    icon = { Icon(painter = painterResource(item.src), contentDescription = null) },
                    label = { Text(text = stringResource(item.titleResId)) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.tertiary
                    )
                )
            }
        }
    }
}

@Composable
private fun MiniAudiPlayer(
    playerState: PlayerState,
    route: String?,
    playbackDurationMs: Long,
    currentPositionMs: Long,
    onPrevious: () -> Unit,
    onPause: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit,
    onClickMiniAudioPlayer: () -> Unit,
    onChangeCurrentPosition: (Long) -> Unit,
) {
    val currentTrack = playerState.currentTrack
    val navigationRotes = remember { navigationItems.map { it.screen.route } }

    AnimatedVisibility(
        visible = currentTrack != null && route in navigationRotes,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        currentTrack?.let { track ->
            MiniAudioPlayer(
                imageUrl = track.smallImageUrl,
                title = track.title,
                artist = track.artist,
                isPlaying = playerState.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(12.dp))
                    .background(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .clickable { onClickMiniAudioPlayer() },
                playbackDurationMs = playbackDurationMs,
                currentPositionMs = currentPositionMs,
                onPrevious = onPrevious,
                onPause = onPause,
                onPlay = onPlay,
                onNext = onNext,
                onChangeCurrentPosition = onChangeCurrentPosition
            )
        }
    }
}