package zechs.zplex.feature_movies.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.BoxWithConstraints
import zechs.zplex.feature_movies.detail.DetailRoute
import zechs.zplex.zplex_api.data.remote.api.MediaListItem
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

@Composable
fun MoviesBrowseRoute(
    onOpenDetail: (MediaType, Int) -> Unit,
    onOpenPlayer: (String) -> Unit,
    viewModel: MoviesBrowseViewModel = hiltViewModel()
) = BrowseRoute(viewModel, onOpenDetail, onOpenPlayer)

@Composable
fun ShowsBrowseRoute(
    onOpenDetail: (MediaType, Int) -> Unit,
    onOpenPlayer: (String) -> Unit,
    viewModel: ShowsBrowseViewModel = hiltViewModel()
) = BrowseRoute(viewModel, onOpenDetail, onOpenPlayer)

@Composable
private fun BrowseRoute(
    viewModel: BrowseViewModel,
    onOpenDetail: (MediaType, Int) -> Unit,
    onOpenPlayer: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagingData.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BrowseEvent.NavigateToDetail -> onOpenDetail(event.mediaType, event.tmdbId)
            }
        }
    }

    BrowseScreen(
        state = state,
        pagingItems = pagingItems,
        onAction = viewModel::onAction,
        onOpenPlayer = onOpenPlayer
    )
}

@Composable
fun BrowseScreen(
    state: BrowseState,
    pagingItems: LazyPagingItems<MediaListItem>,
    onAction: (BrowseAction) -> Unit,
    onOpenPlayer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val twoPane = maxWidth >= 840.dp
        val columns = when {
            maxWidth < 600.dp -> 3
            maxWidth < 840.dp -> 4
            maxWidth < 1200.dp -> 5
            else -> 6
        }

        Column(Modifier.fillMaxSize()) {
            BrowseToolbar(
                state = state,
                onAction = onAction,
                onFilterClick = { showFilterSheet = true }
            )
            if (twoPane) {
                Row(Modifier.fillMaxSize()) {
                    BrowseGridPane(
                        state = state,
                        pagingItems = pagingItems,
                        columns = (columns - 1).coerceAtLeast(2),
                        twoPane = true,
                        onAction = onAction,
                        modifier = Modifier.weight(0.62f)
                    )
                    DetailPane(
                        item = state.selectedItem,
                        mediaType = state.mediaType,
                        onOpenPlayer = onOpenPlayer,
                        modifier = Modifier.weight(0.38f).fillMaxHeight()
                    )
                }
            } else {
                BrowseGridPane(
                    state = state,
                    pagingItems = pagingItems,
                    columns = columns,
                    twoPane = false,
                    onAction = onAction,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showFilterSheet) {
        FilterSheet(
            sections = state.sections,
            current = state.filter,
            onApply = {
                onAction(BrowseAction.ApplyFilter(it))
                showFilterSheet = false
            },
            onClear = {
                onAction(BrowseAction.ClearFilter)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowseToolbar(
    state: BrowseState,
    onAction: (BrowseAction) -> Unit,
    onFilterClick: () -> Unit
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SortMenu(sortBy = state.sortBy, orderBy = state.orderBy, onAction = onAction)
            FilledTonalButton(onClick = onFilterClick) {
                Icon(Icons.Filled.FilterList, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(
                    if (state.activeFilterCount > 0) "Filters (${state.activeFilterCount})"
                    else "Filters"
                )
            }
        }
    }
}

@Composable
private fun DetailPane(
    item: MediaListItem?,
    mediaType: MediaType,
    onOpenPlayer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        tonalElevation = 3.dp
    ) {
        if (item == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Select a title")
            }
        } else {
            DetailRoute(
                mediaType = mediaType,
                tmdbId = item.tmdbId,
                onPlay = onOpenPlayer,
                onBack = {},
                embedded = true,
                viewModel = hiltViewModel(key = "detail_${mediaType.name}_${item.tmdbId}")
            )
        }
    }
}
