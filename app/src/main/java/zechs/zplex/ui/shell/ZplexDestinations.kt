package zechs.zplex.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector

/** Top-level destinations shown in the adaptive navigation suite (bottom bar / nav rail). */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.Home),
    MOVIES("movies", "Movies", Icons.Filled.Movie),
    SHOWS("shows", "Shows", Icons.Filled.Tv),
    DOWNLOADS("downloads", "Downloads", Icons.Filled.Download)
}

/** Shared destinations reachable from any top-level screen. */
object ZplexRoutes {
    const val DETAIL = "detail/{mediaType}/{tmdbId}"

    fun detail(mediaType: String, tmdbId: Int) = "detail/$mediaType/$tmdbId"
}
