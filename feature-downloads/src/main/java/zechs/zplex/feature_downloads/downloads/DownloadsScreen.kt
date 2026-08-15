package zechs.zplex.feature_downloads.downloads

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zechs.zplex.common.player.PlayerArgs
import zechs.zplex.common.ui.state.ZplexEmptyState
import zechs.zplex.common.ui.state.ZplexLoadingState
import zechs.zplex.zplex_api.data.local.downloads.DownloadEntity
import zechs.zplex.zplex_api.data.local.downloads.DownloadStatus
import java.util.Locale

@Composable
fun DownloadsRoute(
    onOpenPlayer: (PlayerArgs) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DownloadsEvent.NavigateToPlayer -> onOpenPlayer(event.args)
            }
        }
    }
    DownloadsScreen(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    state: DownloadsState,
    onAction: (DownloadsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Downloads")
                        if (state.items.isNotEmpty()) {
                            Text(
                                "${formatSize(state.storageUsedBytes)} used",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> ZplexLoadingState(Modifier.padding(padding))
            state.items.isEmpty() -> ZplexEmptyState("No downloads yet", Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 8.dp
                )
            ) {
                items(state.items, key = { it.id }) { item ->
                    DownloadRow(item = item, onAction = onAction)
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(
    item: DownloadEntity,
    onAction: (DownloadsAction) -> Unit
) {
    val playable = item.status == DownloadStatus.COMPLETED
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = playable) { onAction(DownloadsAction.Play(item)) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        item.subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (item.status == DownloadStatus.RUNNING || item.status == DownloadStatus.PAUSED) {
            val fraction = if (item.totalBytes > 0) {
                (item.downloadedBytes.toFloat() / item.totalBytes).coerceIn(0f, 1f)
            } else 0f
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = statusLine(item),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            RowActions(item = item, onAction = onAction)
        }
    }
}

@Composable
private fun RowActions(
    item: DownloadEntity,
    onAction: (DownloadsAction) -> Unit
) {
    when (item.status) {
        DownloadStatus.RUNNING -> {
            IconButton(onClick = { onAction(DownloadsAction.Pause(item.id)) }) {
                Icon(Icons.Filled.Pause, contentDescription = "Pause")
            }
            IconButton(onClick = { onAction(DownloadsAction.Cancel(item.id)) }) {
                Icon(Icons.Filled.Close, contentDescription = "Cancel")
            }
        }

        DownloadStatus.PAUSED, DownloadStatus.QUEUED -> {
            IconButton(onClick = { onAction(DownloadsAction.Resume(item.id)) }) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Resume")
            }
            IconButton(onClick = { onAction(DownloadsAction.Cancel(item.id)) }) {
                Icon(Icons.Filled.Close, contentDescription = "Cancel")
            }
        }

        DownloadStatus.FAILED -> {
            IconButton(onClick = { onAction(DownloadsAction.Resume(item.id)) }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Retry")
            }
            IconButton(onClick = { onAction(DownloadsAction.Delete(item.id)) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }

        DownloadStatus.COMPLETED -> {
            IconButton(onClick = { onAction(DownloadsAction.Play(item)) }) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
            }
            IconButton(onClick = { onAction(DownloadsAction.Delete(item.id)) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

private fun statusLine(item: DownloadEntity): String = when (item.status) {
    DownloadStatus.QUEUED -> "Queued"
    DownloadStatus.RUNNING -> "${formatSize(item.downloadedBytes)} / ${formatSize(item.totalBytes)}"
    DownloadStatus.PAUSED -> "Paused · ${formatSize(item.downloadedBytes)} / ${formatSize(item.totalBytes)}"
    DownloadStatus.COMPLETED -> formatSize(item.totalBytes)
    DownloadStatus.FAILED -> item.errorMessage ?: "Failed"
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return String.format(Locale.US, if (unit == 0) "%.0f %s" else "%.1f %s", value, units[unit])
}
