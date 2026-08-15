package zechs.zplex.feature_search.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zechs.zplex.common.ui.state.ZplexEmptyState
import zechs.zplex.common.ui.state.ZplexErrorState
import zechs.zplex.common.ui.state.ZplexLoadingState
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

@Composable
fun SearchRoute(
    onOpenDetail: (MediaType, Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SearchEvent.NavigateToDetail -> onOpenDetail(event.mediaType, event.tmdbId)
            }
        }
    }
    SearchScreen(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchState,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onAction(SearchAction.QueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search movies & shows") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onAction(SearchAction.ClearQuery) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )
        }
    ) { padding ->
        when {
            state.isLoading -> ZplexLoadingState(Modifier.padding(padding))

            state.error != null && state.suggestions.isEmpty() ->
                ZplexErrorState(
                    message = state.error.message,
                    onRetry = { onAction(SearchAction.Retry) },
                    modifier = Modifier.padding(padding)
                )

            state.query.isBlank() -> SearchList(
                header = "Suggested",
                items = state.suggestions,
                emptyMessage = "No suggestions available",
                onOpen = { onAction(SearchAction.OpenDetail(it.mediaType, it.tmdbId)) },
                modifier = Modifier.padding(padding)
            )

            state.results.isEmpty() ->
                ZplexEmptyState("No matches for \"${state.query.trim()}\"", Modifier.padding(padding))

            else -> SearchList(
                header = null,
                items = state.results,
                emptyMessage = "",
                onOpen = { onAction(SearchAction.OpenDetail(it.mediaType, it.tmdbId)) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun SearchList(
    header: String?,
    items: List<SearchResultItem>,
    emptyMessage: String,
    onOpen: (SearchResultItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty() && emptyMessage.isNotEmpty()) {
        ZplexEmptyState(emptyMessage, modifier)
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        header?.let {
            item(key = "header") {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        items(items = items, key = { "${it.mediaType}_${it.tmdbId}" }) { item ->
            SearchRow(item = item, onClick = { onOpen(item) }, modifier = Modifier.animateItem())
        }
    }
}

@Composable
private fun SearchRow(item: SearchResultItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1
        )
        Text(
            text = item.typeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
