package zechs.zplex.feature_home.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zechs.zplex.common.ui.state.ZplexEmptyState
import zechs.zplex.common.ui.state.ZplexErrorState
import zechs.zplex.common.ui.state.ZplexLoadingState
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

@Composable
fun HomeRoute(
    onOpenDetail: (MediaType, Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.NavigateToDetail -> onOpenDetail(event.mediaType, event.tmdbId)
                is HomeEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    HomeScreen(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(HomeAction.Refresh) },
        modifier = modifier.fillMaxSize()
    ) {
        when {
            state.isLoading -> ZplexLoadingState(Modifier.fillMaxWidth())
            state.error != null && state.rows.isEmpty() ->
                ZplexErrorState(state.error.message, onRetry = { onAction(HomeAction.Refresh) })

            state.rows.isEmpty() -> ZplexEmptyState("Nothing to watch yet")
            else -> HomeContent(state.rows, onAction)
        }
    }
}

@Composable
private fun HomeContent(
    rows: List<HomeRow>,
    onAction: (HomeAction) -> Unit
) {
    val expanded = LocalConfiguration.current.screenWidthDp >= 840
    val posterWidth = if (expanded) 150.dp else 118.dp
    val heroItems = rows.filterIsInstance<HomeRow.Latest>()
        .firstOrNull()?.items?.take(5).orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (heroItems.isNotEmpty()) {
            item(key = "hero") {
                HeroCarousel(
                    items = heroItems,
                    onClick = { card -> onAction(HomeAction.OpenDetail(card.mediaType, card.tmdbId)) }
                )
            }
        }
        items(items = rows, key = { it.key }) { row ->
            when (row) {
                is HomeRow.ContinueWatching -> ContinueWatchingRail(
                    items = row.items,
                    onClick = { card -> onAction(HomeAction.OpenDetail(card.mediaType, card.tmdbId)) },
                    onRemove = { card -> onAction(HomeAction.RemoveContinueWatching(card.id)) }
                )

                is HomeRow.Latest -> PosterRail(
                    title = row.title,
                    items = row.items,
                    posterWidth = posterWidth,
                    onClick = { card -> onAction(HomeAction.OpenDetail(card.mediaType, card.tmdbId)) }
                )
            }
        }
    }
}

@Composable
internal fun SectionHeader(title: String) {
    androidx.compose.material3.Text(
        text = title,
        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
