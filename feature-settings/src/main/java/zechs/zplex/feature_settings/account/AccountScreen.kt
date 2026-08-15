package zechs.zplex.feature_settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zechs.zplex.common.ui.state.ZplexLoadingState
import zechs.zplex.common.ui.theme.ThemeMode

@Composable
fun AccountRoute(
    onOpenHistory: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenProfiles: () -> Unit,
    onOpenKidsMode: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AccountEvent.NavigateToHistory -> onOpenHistory()
                AccountEvent.NavigateToAdmin -> onOpenAdmin()
                AccountEvent.NavigateToProfiles -> onOpenProfiles()
                AccountEvent.NavigateToKidsMode -> onOpenKidsMode()
                is AccountEvent.ShowMessage -> Unit
            }
        }
    }
    AccountScreen(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    state: AccountState,
    onAction: (AccountAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Account") }) }
    ) { padding ->
        if (state.isLoading) {
            ZplexLoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ProfileCard(state) }
            item { CapabilitiesCard(state.capabilityLabels) }
            item { ThemeCard(state.themeMode, onAction) }
            item { ServerInfoCard(state) }
            item {
                LinksCard(
                    isAdmin = state.isAdmin,
                    onAction = onAction
                )
            }
            item {
                OutlinedButton(
                    onClick = { onAction(AccountAction.RequestLogout) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null)
                    Text("  Log out")
                }
            }
        }
    }

    if (state.showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { onAction(AccountAction.DismissLogoutConfirm) },
            title = { Text("Log out?") },
            text = { Text("You'll need to sign in again to continue watching.") },
            confirmButton = {
                TextButton(onClick = { onAction(AccountAction.ConfirmLogout) }) { Text("Log out") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(AccountAction.DismissLogoutConfirm) }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileCard(state: AccountState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "${state.firstName} ${state.lastName}".trim(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "@${state.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.isAdult) {
                Text(
                    "Adult content enabled",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CapabilitiesCard(labels: List<String>) {
    if (labels.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Capabilities", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                labels.forEach { label ->
                    AssistChip(onClick = {}, label = { Text(label) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeCard(themeMode: ThemeMode, onAction: (AccountAction) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Theme", style = MaterialTheme.typography.titleSmall)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = themeMode == mode,
                        onClick = { onAction(AccountAction.SetTheme(mode)) },
                        shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size)
                    ) {
                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerInfoCard(state: AccountState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Server", style = MaterialTheme.typography.titleSmall)
            Text(
                state.streamingHost ?: "Not configured",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Text(
                "App version ${state.appVersion}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LinksCard(isAdmin: Boolean, onAction: (AccountAction) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            ListItem(
                headlineContent = { Text("Watch history") },
                leadingContent = { Icon(Icons.Filled.History, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAction(AccountAction.OpenHistory) }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Switch profile") },
                leadingContent = { Icon(Icons.Filled.SwitchAccount, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAction(AccountAction.OpenProfiles) }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Kids mode") },
                leadingContent = { Icon(Icons.Filled.ChildCare, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAction(AccountAction.OpenKidsMode) }
            )
            if (isAdmin) {
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Admin") },
                    leadingContent = { Icon(Icons.Filled.AdminPanelSettings, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(AccountAction.OpenAdmin) }
                )
            }
        }
    }
}
