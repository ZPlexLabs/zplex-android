package zechs.zplex.feature_admin.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zechs.zplex.common.ui.state.ZplexErrorState
import zechs.zplex.common.ui.state.ZplexLoadingState
import zechs.zplex.zplex_api.data.remote.api.admin.model.BlacklistEntry
import zechs.zplex.zplex_api.data.remote.api.suggestions.SearchSuggestion

@Composable
fun AdminEditRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AdminEditEvent.NavigateBack -> onBack()
                is AdminEditEvent.ShowMessage -> Unit
            }
        }
    }
    AdminEditScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditScreen(
    state: AdminEditState,
    onAction: (AdminEditAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("@${state.username}") }) }
    ) { padding ->
        when {
            state.isLoading -> ZplexLoadingState(Modifier.padding(padding))
            state.error != null -> ZplexErrorState(
                message = state.error.message,
                onRetry = { onAction(AdminEditAction.Retry) },
                modifier = Modifier.padding(padding)
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { CapabilitiesSection(state, onAction) }
                item { AccessSection(state, onAction) }
                item { BlacklistSection(state, onAction) }
                item {
                    androidx.compose.material3.Button(
                        onClick = { onAction(AdminEditAction.Save) },
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (state.isSaving) "Saving…" else "Save changes") }
                }
                item {
                    OutlinedButton(
                        onClick = { onAction(AdminEditAction.RequestDelete) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Delete user") }
                }
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { onAction(AdminEditAction.DismissDeleteConfirm) },
            title = { Text("Delete @${state.username}?") },
            text = { Text("This permanently removes the account.") },
            confirmButton = {
                TextButton(onClick = { onAction(AdminEditAction.ConfirmDelete) }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(AdminEditAction.DismissDeleteConfirm) }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CapabilitiesSection(state: AdminEditState, onAction: (AdminEditAction) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Capabilities", style = MaterialTheme.typography.titleSmall)
            state.allCapabilities.forEach { capability ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(AdminEditAction.ToggleCapability(capability.id)) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = capability.id in state.selectedCapabilityIds,
                        onCheckedChange = { onAction(AdminEditAction.ToggleCapability(capability.id)) }
                    )
                    Column {
                        Text(capability.label)
                        Text(
                            capability.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessSection(state: AdminEditState, onAction: (AdminEditAction) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Access", style = MaterialTheme.typography.titleSmall)

            Text("Libraries", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LIBRARY_LABELS.forEach { (id, label) ->
                    FilterChip(
                        selected = id in state.allowedLibraries,
                        onClick = { onAction(AdminEditAction.ToggleLibrary(id)) },
                        label = { Text(label) }
                    )
                }
            }

            Text("Rating ceiling", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RATING_RANK_LABELS.forEach { (rank, label) ->
                    FilterChip(
                        selected = state.maxRatingRank == rank,
                        onClick = { onAction(AdminEditAction.SetRatingRank(rank)) },
                        label = { Text(label) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Allow unrated titles")
                Switch(
                    checked = state.allowUnrated,
                    onCheckedChange = { onAction(AdminEditAction.SetAllowUnrated(it)) }
                )
            }
        }
    }
}

@Composable
private fun BlacklistSection(state: AdminEditState, onAction: (AdminEditAction) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Blacklist", style = MaterialTheme.typography.titleSmall)

            state.blacklist.forEach { entry ->
                BlacklistRow(entry, onAction)
            }

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onAction(AdminEditAction.QueryChanged(it)) },
                label = { Text("Search catalog to blacklist") },
                modifier = Modifier.fillMaxWidth()
            )
            state.searchResults.forEach { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(AdminEditAction.AddToBlacklist(suggestion)) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(suggestion.title, modifier = Modifier.weight(1f))
                    Text(
                        suggestion.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BlacklistRow(entry: BlacklistEntry, onAction: (AdminEditAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${entry.mediaType.name} #${entry.tmdbId}")
        IconButton(onClick = { onAction(AdminEditAction.RemoveFromBlacklist(entry)) }) {
            Icon(Icons.Filled.Close, contentDescription = "Remove")
        }
    }
    HorizontalDivider()
}
