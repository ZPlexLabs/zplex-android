package zechs.zplex.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import zechs.zplex.feature_home.home.HomeRoute
import zechs.zplex.feature_movies.browse.MoviesBrowseRoute
import zechs.zplex.feature_movies.browse.ShowsBrowseRoute
import zechs.zplex.feature_movies.detail.DetailRoute
import zechs.zplex.feature_player.PlayerActivity
import zechs.zplex.feature_search.search.SearchRoute
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

/** Navigation-Compose graph wiring the top-level and shared destinations. */
@Composable
fun ZplexNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
        composable(TopLevelDestination.MOVIES.route) {
            MoviesBrowseRoute(
                onOpenDetail = { mediaType, tmdbId ->
                    navController.navigate(ZplexRoutes.detail(mediaType.name.lowercase(), tmdbId))
                },
                onOpenPlayer = { args -> context.startActivity(PlayerActivity.newIntent(context, args)) }
            )
        }
        composable(TopLevelDestination.SHOWS.route) {
            ShowsBrowseRoute(
                onOpenDetail = { mediaType, tmdbId ->
                    navController.navigate(ZplexRoutes.detail(mediaType.name.lowercase(), tmdbId))
                },
                onOpenPlayer = { args -> context.startActivity(PlayerActivity.newIntent(context, args)) }
            )
        }
        composable(TopLevelDestination.SEARCH.route) {
            SearchRoute(
                onOpenDetail = { mediaType, tmdbId ->
                    navController.navigate(ZplexRoutes.detail(mediaType.name.lowercase(), tmdbId))
                }
            )
        }
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
            DetailRoute(
                mediaType = MediaType.valueOf(mediaType.uppercase()),
                tmdbId = tmdbId,
                onPlay = { args -> context.startActivity(PlayerActivity.newIntent(context, args)) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(label)
    }
}
