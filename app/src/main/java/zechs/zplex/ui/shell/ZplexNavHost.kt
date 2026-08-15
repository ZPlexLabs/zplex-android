package zechs.zplex.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import zechs.zplex.feature_home.home.HomeRoute

/** Navigation-Compose graph wiring the top-level and shared destinations. */
@Composable
fun ZplexNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.HOME.route,
        modifier = modifier
    ) {
        composable(TopLevelDestination.HOME.route) {
            HomeRoute(
                onOpenDetail = { mediaType, tmdbId ->
                    navController.navigate(ZplexRoutes.detail(mediaType.name.lowercase(), tmdbId))
                }
            )
        }
        composable(TopLevelDestination.MOVIES.route) { PlaceholderScreen("Movies") }
        composable(TopLevelDestination.SHOWS.route) { PlaceholderScreen("Shows") }
        composable(TopLevelDestination.DOWNLOADS.route) { PlaceholderScreen("Downloads") }

        composable(
            route = ZplexRoutes.DETAIL,
            arguments = listOf(
                navArgument("mediaType") { type = NavType.StringType },
                navArgument("tmdbId") { type = NavType.IntType }
            )
        ) { entry ->
            val mediaType = entry.arguments?.getString("mediaType").orEmpty()
            val tmdbId = entry.arguments?.getInt("tmdbId") ?: 0
            PlaceholderScreen("Detail · $mediaType · $tmdbId")
        }

        composable(
            route = ZplexRoutes.PLAYER,
            arguments = listOf(navArgument("fileId") { type = NavType.StringType })
        ) { entry ->
            val fileId = entry.arguments?.getString("fileId").orEmpty()
            PlaceholderScreen("Player · $fileId")
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(label)
    }
}
