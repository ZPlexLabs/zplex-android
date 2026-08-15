package zechs.zplex.ui.shell

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import zechs.zplex.common.ui.theme.ZplexTheme

/**
 * Adaptive navigation shell: bottom bar on compact widths, nav rail on larger screens.
 * Shared detail/player destinations render full-screen without the navigation suite.
 */
@Composable
fun ZplexAppShell(
    navController: NavHostController = rememberNavController()
) {
    ZplexTheme {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination
        val isTopLevel = TopLevelDestination.entries.any { destination ->
            currentDestination?.hierarchy?.any { it.route == destination.route } == true
        }

        val layoutType = if (isTopLevel) {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
        } else {
            NavigationSuiteType.None
        }

        NavigationSuiteScaffold(
            layoutType = layoutType,
            navigationSuiteItems = {
                TopLevelDestination.entries.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == destination.route
                    } == true
                    item(
                        selected = selected,
                        onClick = { navController.navigateToTopLevel(destination) },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        ) {
            ZplexNavHost(navController = navController)
        }
    }
}

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
