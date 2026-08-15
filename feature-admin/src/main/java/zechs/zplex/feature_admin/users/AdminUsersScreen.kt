package zechs.zplex.feature_admin.users

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.snackbar.LocalSnackbarHostState
import zechs.zplex.common.ui.state.ZplexEmptyState
import zechs.zplex.common.ui.state.ZplexErrorState
import zechs.zplex.common.ui.state.ZplexLoadingState
import zechs.zplex.zplex_api.data.remote.api.admin.model.UserSummaryResponse

private const val ADMIN_CAPABILITY_ID = 6

@Composable
fun AdminUsersRoute(
    onOpenUser: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminUsersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AdminUsersEvent.OpenUserEdit -> onOpenUser(event.username)
                is AdminUsersEvent.ShowMessage ->
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
            }
        }
    }
    AdminUsersScreen(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    state: AdminUsersState,
    onAction: (AdminUsersAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Admin · Users") }) }
    ) { padding ->
        when {
            state.isLoading -> ZplexLoadingState(Modifier.padding(padding))
            state.error != null && state.users.isEmpty() -> ZplexErrorState(
                message = state.error.message,
                onRetry = { onAction(AdminUsersAction.Retry) },
                modifier = Modifier.padding(padding)
            )

            state.users.isEmpty() -> ZplexEmptyState("No users yet", Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 8.dp
                )
            ) {
                items(state.users, key = { it.username }) { user ->
                    UserRow(
                        user = user,
                        onAction = onAction,
                        modifier = Modifier.animateItem()
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    val pending = state.pendingDeleteUsername
    if (pending != null) {
        val haptic = LocalHapticFeedback.current
        AlertDialog(
            onDismissRequest = { onAction(AdminUsersAction.DismissDeleteConfirm) },
            title = { Text("Delete $pending?") },
            text = { Text("This permanently removes the account.") },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAction(AdminUsersAction.ConfirmDelete)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(AdminUsersAction.DismissDeleteConfirm) }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun UserRow(
    user: UserSummaryResponse,
    onAction: (AdminUsersAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAction(AdminUsersAction.OpenUser(user.username)) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "${user.firstName} ${user.lastName}".trim(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "@${user.username}" + if (ADMIN_CAPABILITY_ID in user.capabilities) " · Admin" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { onAction(AdminUsersAction.RequestDelete(user.username)) }) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete ${user.username}")
        }
    }
}
