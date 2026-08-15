package zechs.zplex.feature_movies.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Seasons + episodes UI is implemented in the show-detail task.
@Composable
internal fun ShowSeasonsSection(
    state: DetailState,
    accent: Color,
    onAction: (DetailAction) -> Unit
) {
    if (state.seasons.isEmpty()) return
}
