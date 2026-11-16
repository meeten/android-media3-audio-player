package com.example.mediastoreexampleapp.app.bottomnavigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mediastoreexampleapp.app.navigation.NavigationState
import com.example.mediastoreexampleapp.app.ui.MiniAudioPlayer
import com.example.mediastoreexampleapp.domain.model.Track

private val navigationItems = listOf(
    NavigationItem.RemoteTracks,
    NavigationItem.LocalTracks,
)


@Composable
fun AppNavigationBottomBar(
    navigationState: NavigationState,
    currentTrack: Track?,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry = navigationState.navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    Column(modifier = modifier) {
        MiniAudiPlayer(currentTrack = currentTrack, route = currentDestination?.route)

        BottomAppBar(
            containerColor = Color.White
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
                )
            }
        }
    }
}

@Composable
private fun MiniAudiPlayer(currentTrack: Track?, route: String?) {
    val routes = navigationItems.map { it.screen.route }

    if (currentTrack != null && route in routes)
        MiniAudioPlayer(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(color = Color.Red)
        )
}