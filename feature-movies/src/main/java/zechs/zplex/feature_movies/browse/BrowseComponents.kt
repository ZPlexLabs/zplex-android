package zechs.zplex.feature_movies.browse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.state.ZplexCircularLoading
import zechs.zplex.common.ui.state.ZplexEmptyState
import zechs.zplex.common.ui.state.ZplexErrorState
import zechs.zplex.common.utils.TmdbImage
import zechs.zplex.zplex_api.data.browse.FilterQuery
import zechs.zplex.zplex_api.data.remote.api.MediaListItem
import zechs.zplex.zplex_api.data.remote.api.enums.OrderBy
import kotlin.math.roundToInt

@Composable
internal fun SortMenu(
    sortBy: zechs.zplex.zplex_api.data.remote.api.enums.SortBy,
    orderBy: OrderBy,
    onAction: (BrowseAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilledTonalButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.Sort, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(BROWSE_SORT_OPTIONS.firstOrNull { it.sortBy == sortBy }?.label ?: sortBy.name)
            Icon(
                imageVector = if (orderBy == OrderBy.DESC) Icons.Filled.ArrowDownward
                else Icons.Filled.ArrowUpward,
                contentDescription = null
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BROWSE_SORT_OPTIONS.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    leadingIcon = {
                        if (option.sortBy == sortBy) Icon(Icons.Filled.Check, contentDescription = null)
                    },
                    onClick = {
                        val newOrder = if (option.sortBy == sortBy) orderBy.flipped() else orderBy
                        onAction(BrowseAction.SetSort(option.sortBy, newOrder))
                        expanded = false
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(if (orderBy == OrderBy.DESC) "Descending" else "Ascending") },
                leadingIcon = {
                    Icon(
                        if (orderBy == OrderBy.DESC) Icons.Filled.ArrowDownward
                        else Icons.Filled.ArrowUpward,
                        contentDescription = null
                    )
                },
                onClick = {
                    onAction(BrowseAction.SetSort(sortBy, orderBy.flipped()))
                    expanded = false
                }
            )
        }
    }
}

@Composable
internal fun BrowseGridPane(
    state: BrowseState,
    pagingItems: LazyPagingItems<MediaListItem>,
    columns: Int,
    twoPane: Boolean,
    onAction: (BrowseAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val refresh = pagingItems.loadState.refresh

    Box(modifier) {
        when {
            refresh is LoadState.Loading && pagingItems.itemCount == 0 -> ZplexCircularLoading()

            refresh is LoadState.Error && pagingItems.itemCount == 0 -> ZplexErrorState(
                message = refresh.error.message ?: "Something went wrong",
                onRetry = { pagingItems.retry() }
            )

            refresh is LoadState.NotLoading && pagingItems.itemCount == 0 ->
                ZplexEmptyState("No titles found")

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = gridState,
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = pagingItems.itemKey { it.tmdbId }
                ) { index ->
                    val item = pagingItems[index] ?: return@items
                    PosterGridItem(
                        item = item,
                        selected = twoPane && state.selectedItem?.tmdbId == item.tmdbId,
                        onClick = {
                            if (twoPane) onAction(BrowseAction.SelectItem(item))
                            else onAction(BrowseAction.OpenDetail(item))
                        }
                    )
                }
                if (pagingItems.loadState.append is LoadState.Loading) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }
            }
        }

        if (gridState.canScrollForward || gridState.canScrollBackward) {
            FastScrollbar(
                gridState = gridState,
                itemCount = pagingItems.itemCount,
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun PosterGridItem(
    item: MediaListItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(10.dp))
                .then(
                    if (selected) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                    else Modifier
                )
        ) {
            AsyncImage(
                model = TmdbImage.poster(item.posterPath),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (selected) Modifier.padding(3.dp) else Modifier
                    )
            )
            item.imdbRating?.let { rating ->
                RatingBadge(
                    rating = rating,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                )
            }
        }
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp, start = 2.dp, end = 2.dp)
        )
    }
}

@Composable
private fun RatingBadge(rating: Double, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = String.format("%.1f", rating),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
internal fun DetailPaneContent(item: MediaListItem) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = TmdbImage.poster(item.posterPath, size = "w500"),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(180.dp)
                .aspectRatio(2f / 3f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clip(RoundedCornerShape(12.dp))
        )
        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp)
        )
        val meta = buildList {
            if (item.release.isNotBlank()) add(item.release)
            item.imdbRating?.let { add("★ ${String.format("%.1f", it)}") }
        }.joinToString(" · ")
        if (meta.isNotEmpty()) {
            Text(
                text = meta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        Text(
            text = "Full details arrive in a later milestone.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}

@Composable
private fun FastScrollbar(
    gridState: LazyGridState,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    if (itemCount == 0) return
    val scope = rememberCoroutineScope()
    var dragging by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val thumbHeight = 48.dp
    val thumbHeightPx = with(density) { thumbHeight.toPx() }

    BoxWithConstraints(modifier.width(20.dp)) {
        val trackHeightPx = constraints.maxHeight.toFloat()
        val progress = gridState.firstVisibleItemIndex.toFloat() / itemCount.coerceAtLeast(1)
        val offsetY = (progress * (trackHeightPx - thumbHeightPx))
            .coerceIn(0f, (trackHeightPx - thumbHeightPx).coerceAtLeast(0f))

        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .width(6.dp)
                .height(thumbHeight)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = if (dragging) 0.9f else 0.45f)
                )
                .pointerInput(itemCount, trackHeightPx) {
                    detectVerticalDragGestures(
                        onDragStart = { dragging = true },
                        onDragEnd = { dragging = false },
                        onDragCancel = { dragging = false }
                    ) { change, dragAmount ->
                        change.consume()
                        val current = gridState.firstVisibleItemIndex.toFloat() /
                            itemCount.coerceAtLeast(1)
                        val deltaFraction = dragAmount /
                            (trackHeightPx - thumbHeightPx).coerceAtLeast(1f)
                        val target = ((current + deltaFraction) * itemCount).roundToInt()
                            .coerceIn(0, itemCount - 1)
                        scope.launch { gridState.scrollToItem(target) }
                    }
                }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun FilterSheet(
    sections: List<FilterSection>,
    current: FilterQuery,
    onApply: (FilterQuery) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember { mutableStateOf(current) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filters", style = MaterialTheme.typography.titleLarge)
                TextButton(
                    onClick = { draft = FilterQuery() },
                    enabled = !draft.isEmpty
                ) { Text("Clear") }
            }

            Column(
                Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                sections.forEach { section ->
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        section.options.forEach { option ->
                            val selected = draft.isSelected(section.key, option.id)
                            FilterChip(
                                selected = selected,
                                onClick = { draft = draft.toggle(section.key, option.id) },
                                label = { Text(option.label) },
                                leadingIcon = if (selected) {
                                    { Icon(Icons.Filled.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }
            }

            FilledTonalButton(
                onClick = {
                    if (draft.isEmpty) onClear() else onApply(draft)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) { Text("Apply") }
        }
    }
}

private fun OrderBy.flipped(): OrderBy =
    if (this == OrderBy.DESC) OrderBy.ASC else OrderBy.DESC

private fun FilterQuery.isSelected(key: SectionKey, id: String): Boolean = when (key) {
    SectionKey.GENRES -> id.toIntOrNull()?.let { it in genreIds } ?: false
    SectionKey.STUDIOS -> id.toIntOrNull()?.let { it in studioIds } ?: false
    SectionKey.YEARS -> id.toIntOrNull()?.let { it in years } ?: false
    SectionKey.RATINGS -> id in parentalRatings
}

private fun FilterQuery.toggle(key: SectionKey, id: String): FilterQuery = when (key) {
    SectionKey.GENRES -> copy(genreIds = genreIds.toggleInt(id))
    SectionKey.STUDIOS -> copy(studioIds = studioIds.toggleInt(id))
    SectionKey.YEARS -> copy(years = years.toggleInt(id))
    SectionKey.RATINGS -> copy(
        parentalRatings = if (id in parentalRatings) parentalRatings - id else parentalRatings + id
    )
}

private fun Set<Int>.toggleInt(id: String): Set<Int> {
    val value = id.toIntOrNull() ?: return this
    return if (value in this) this - value else this + value
}
