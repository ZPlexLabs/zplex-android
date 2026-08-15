package zechs.zplex.feature_settings.profiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zechs.zplex.common.ui.state.ZplexEmptyState
import zechs.zplex.common.ui.state.ZplexLoadingState
import zechs.zplex.zplex_api.data.local.accounts.SavedAccount

@Composable
fun ProfilesRoute(
    modifier: Modifier = Modifier,
    viewModel: ProfilesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfilesScreen(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    state: ProfilesState,
    onAction: (ProfilesAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Switch profile") }) }
    ) { padding ->
        when {
            state.isLoading -> ZplexLoadingState(Modifier.padding(padding))
            state.accounts.isEmpty() -> ZplexEmptyState("No saved accounts", Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 8.dp
                )
            ) {
                items(state.accounts, key = { it.username }) { account ->
                    ProfileRow(
                        account = account,
                        isActive = account.username == state.activeUsername,
                        onAction = onAction
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text("Add account") },
                        leadingContent = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAction(ProfilesAction.RequestAddAccount) }
                    )
                }
            }
        }
    }

    if (state.pendingRemoveUsername != null) {
        AlertDialog(
            onDismissRequest = { onAction(ProfilesAction.DismissRemove) },
            title = { Text("Remove @${state.pendingRemoveUsername}?") },
            text = { Text("You'll need to sign in again to add it back.") },
            confirmButton = {
                TextButton(onClick = { onAction(ProfilesAction.ConfirmRemove) }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ProfilesAction.DismissRemove) }) { Text("Cancel") }
            }
        )
    }

    if (state.showAddAccountConfirm) {
        AlertDialog(
            onDismissRequest = { onAction(ProfilesAction.DismissAddAccount) },
            title = { Text("Add another account?") },
            text = { Text("You'll be signed out to log in with a different account. Saved accounts stay available.") },
            confirmButton = {
                TextButton(onClick = { onAction(ProfilesAction.ConfirmAddAccount) }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ProfilesAction.DismissAddAccount) }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileRow(
    account: SavedAccount,
    isActive: Boolean,
    onAction: (ProfilesAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isActive) { onAction(ProfilesAction.SwitchTo(account)) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${account.firstName} ${account.lastName}".trim())
            Text(
                "@${account.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isActive) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Active",
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            IconButton(onClick = { onAction(ProfilesAction.RequestRemove(account.username)) }) {
                Icon(Icons.Filled.Close, contentDescription = "Remove")
            }
        }
    }
}
