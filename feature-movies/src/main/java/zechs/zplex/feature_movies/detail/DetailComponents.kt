package zechs.zplex.feature_movies.detail

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.LaunchedEffect
import zechs.zplex.common.utils.TmdbImage
import zechs.zplex.zplex_api.data.remote.api.media.Cast
import zechs.zplex.zplex_api.data.remote.api.media.Crew
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistResponse

@Composable
internal fun rememberAccentColor(imageUrl: String?, fallback: Color): Color {
    val context = LocalContext.current
    var accent by remember(imageUrl) { mutableStateOf(fallback) }
    LaunchedEffect(imageUrl) {
        if (imageUrl.isNullOrBlank()) return@LaunchedEffect
        val bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .size(240)
                    .build()
                (context.imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
            }.getOrNull()
        } ?: return@LaunchedEffect
        val swatch = withContext(Dispatchers.Default) {
            val palette = Palette.from(bitmap).generate()
            palette.vibrantSwatch ?: palette.mutedSwatch ?: palette.dominantSwatch
        }
        swatch?.let { accent = Color(it.rgb) }
    }
    return accent
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MetaChips(meta: List<String>) {
    if (meta.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        meta.forEach { value ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
internal fun DetailActions(
    state: DetailState,
    accent: Color,
    onAction: (DetailAction) -> Unit
) {
    val onAccent = if (accent.luminance() > 0.5f) Color.Black else Color.White
    Button(
        onClick = { onAction(DetailAction.Play) },
        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(state.resume?.label ?: "Play")
    }

    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ActionIcon(
            icon = if (state.inWatchlist) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
            label = "Watchlist",
            selected = state.inWatchlist,
            accent = accent,
            onClick = { onAction(DetailAction.ToggleWatchlist) }
        )
        ActionIcon(
            icon = if (state.isPlayed) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
            label = "Played",
            selected = state.isPlayed,
            accent = accent,
            onClick = { onAction(DetailAction.TogglePlayed) }
        )
        ActionIcon(
            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
            label = "Playlist",
            selected = false,
            accent = accent,
            onClick = { onAction(DetailAction.ShowPlaylistPicker) }
        )
        if (state.trailerUrl != null) {
            ActionIcon(
                icon = Icons.Filled.OndemandVideo,
                label = "Trailer",
                selected = false,
                accent = accent,
                onClick = { onAction(DetailAction.OpenTrailer) }
            )
        }
        ActionIcon(
            icon = Icons.Filled.Download,
            label = "Download",
            selected = false,
            accent = accent,
            onClick = { onAction(DetailAction.Download) }
        )
    }
}

@Composable
private fun ActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = if (selected) accent else MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(icon, contentDescription = label)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LabeledChips(label: String, values: List<String>, accent: Color) {
    if (values.isEmpty()) return
    Spacer(Modifier.height(14.dp))
    Text(text = label, style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(6.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { value ->
            Surface(
                color = accent.copy(alpha = 0.14f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
internal fun CastRail(cast: List<Cast>) {
    if (cast.isEmpty()) return
    Spacer(Modifier.height(18.dp))
    Text(text = "Cast", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(cast) { person ->
            PersonCard(imageUrl = person.image, name = person.name, subtitle = person.role)
        }
    }
}

@Composable
internal fun CrewRail(crew: List<Crew>) {
    if (crew.isEmpty()) return
    Spacer(Modifier.height(18.dp))
    Text(text = "Crew", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(crew) { person ->
            PersonCard(imageUrl = person.image, name = person.name, subtitle = person.job)
        }
    }
}

@Composable
private fun PersonCard(imageUrl: String?, name: String?, subtitle: String?) {
    Column(
        modifier = Modifier.width(76.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = profileImage(imageUrl),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Text(
            text = name.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
        subtitle?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaylistPickerSheet(
    playlists: List<PlaylistResponse>,
    onPick: (Long) -> Unit,
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newName by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Text("Add to playlist", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New playlist") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                FilledTonalIconButton(
                    onClick = { if (newName.isNotBlank()) onCreate(newName.trim()) },
                    enabled = newName.isNotBlank()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Create playlist")
                }
            }
            Spacer(Modifier.height(8.dp))
            playlists.forEach { playlist ->
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(playlist.id) }
                        .padding(vertical = 14.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun profileImage(image: String?): String? = when {
    image.isNullOrBlank() -> null
    image.startsWith("http") -> image
    else -> TmdbImage.poster(image, size = "w185")
}
