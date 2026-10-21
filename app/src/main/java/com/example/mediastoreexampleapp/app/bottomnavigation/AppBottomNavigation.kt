package com.example.mediastoreexampleapp.app.bottomnavigation

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mediastoreexampleapp.app.navigation.NavigationState

@Composable
fun AppNavigationBottomBar(
    navigationState: NavigationState,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry = navigationState.navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    val navigationItems = listOf(
        NavigationItem.RemoteTracks,
        NavigationItem.LocalTracks,
    )

    BottomAppBar(modifier = modifier) {
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