package zechs.zplex.feature_movies.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import zechs.zplex.common.utils.TmdbImage
import zechs.zplex.zplex_api.data.remote.api.tvshows.Season

@Composable
internal fun ShowSeasonsSection(
    state: DetailState,
    accent: Color,
    onAction: (DetailAction) -> Unit
) {
    if (state.seasons.isEmpty()) return
    val selectedSeason = state.seasons.firstOrNull { it.id == state.selectedSeasonId }

    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Episodes", style = MaterialTheme.typography.titleMedium)
        SeasonSelector(
            seasons = state.seasons,
            selectedName = selectedSeason?.name ?: "Season",
            onSelect = { onAction(DetailAction.SelectSeason(it)) }
        )
    }

    state.nextUp?.let { next ->
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { onAction(DetailAction.PlayEpisode(next)) },
            colors = ButtonDefaults.filledTonalButtonColors(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.PlayCircle, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(nextUpLabel(next))
        }
    }

    Spacer(Modifier.height(8.dp))
    when {
        state.episodesLoading -> Box(
            Modifier.fillMaxWidth().padding(24.dp),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        else -> state.episodes.forEach { row ->
            EpisodeItem(row = row, accent = accent, onAction = onAction)
        }
    }
}

@Composable
private fun SeasonSelector(
    seasons: List<Season>,
    selectedName: String,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedName, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            seasons.forEach { season ->
                DropdownMenuItem(
                    text = { Text(season.name ?: "Season ${season.seasonNumber}") },
                    onClick = {
                        expanded = false
                        onSelect(season.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun EpisodeItem(
    row: EpisodeRow,
    accent: Color,
    onAction: (DetailAction) -> Unit
) {
    val episode = row.episode
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(68.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onAction(DetailAction.PlayEpisode(row)) },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = TmdbImage.backdrop(episode.stillPath, size = "w300"),
                contentDescription = episode.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(68.dp)
            )
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Play episode",
                tint = Color.White
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = episodeTitle(row),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            episode.overview?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (row.progressMs > 0 && row.durationMs > 0) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { row.fraction },
                    color = accent,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50))
                )
            }
        }
        IconButton(onClick = { onAction(DetailAction.ToggleEpisodePlayed(row)) }) {
            Icon(
                imageVector = if (row.played) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = if (row.played) "Mark unwatched" else "Mark watched",
                tint = if (row.played) accent else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun episodeTitle(row: EpisodeRow): String {
    val number = row.episode.episodeNumber?.toInt()
    val title = row.episode.title.orEmpty()
    return if (number != null) "$number. $title" else title
}

private fun nextUpLabel(row: EpisodeRow): String {
    val season = row.episode.seasonNumber?.toInt()
    val episode = row.episode.episodeNumber?.toInt()
    val verb = if (row.progressMs > 0) "Resume" else "Play"
    return if (season != null && episode != null) "$verb S${season}E$episode" else verb
}
