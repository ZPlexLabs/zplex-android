package zechs.zplex.feature_movies.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import zechs.zplex.common.ui.state.ZplexCircularLoading
import zechs.zplex.common.ui.state.ZplexErrorState
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

@Composable
fun DetailRoute(
    mediaType: MediaType,
    tmdbId: Int,
    onPlay: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    embedded: Boolean = false,
    viewModel: MediaDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(mediaType, tmdbId) { viewModel.load(mediaType, tmdbId) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.NavigateToPlayer -> onPlay(event.fileId)
                is DetailEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()

                is DetailEvent.OpenUrl -> runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }.onFailure {
                    Toast.makeText(context, "Can't open link", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    DetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        embedded = embedded,
        modifier = modifier
    )
}

@Composable
fun DetailScreen(
    state: DetailState,
    onAction: (DetailAction) -> Unit,
    onBack: () -> Unit,
    embedded: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(modifier.fillMaxSize()) {
        when {
            state.isLoading && state.header == null -> ZplexCircularLoading()

            state.error != null && state.header == null -> ZplexErrorState(
                message = state.error.message,
                onRetry = { onAction(DetailAction.Retry) }
            )

            state.header != null -> DetailContent(
                state = state,
                onAction = onAction,
                onBack = onBack,
                embedded = embedded
            )
        }
    }

    if (state.showPlaylistPicker) {
        PlaylistPickerSheet(
            playlists = state.playlists,
            onPick = { onAction(DetailAction.AddToPlaylist(it)) },
            onCreate = { onAction(DetailAction.CreatePlaylistAndAdd(it)) },
            onDismiss = { onAction(DetailAction.DismissPlaylistPicker) }
        )
    }
}

@Composable
private fun DetailContent(
    state: DetailState,
    onAction: (DetailAction) -> Unit,
    onBack: () -> Unit,
    embedded: Boolean
) {
    val header = state.header ?: return
    val accent = rememberAccentColor(
        imageUrl = header.backdropUrl ?: header.posterUrl,
        fallback = MaterialTheme.colorScheme.primary
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(Modifier.fillMaxWidth().height(if (embedded) 220.dp else 300.dp)) {
            AsyncImage(
                model = header.backdropUrl ?: header.posterUrl,
                contentDescription = header.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            1f to MaterialTheme.colorScheme.surface
                        )
                    )
            )
            if (!embedded) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.statusBarsPadding().padding(4.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
            Box(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                if (header.logoUrl != null) {
                    AsyncImage(
                        model = header.logoUrl,
                        contentDescription = header.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.heightIn(max = 84.dp).fillMaxWidth(0.6f)
                    )
                } else {
                    Text(
                        text = header.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
            }
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            MetaChips(header.meta)

            state.resume?.let { resume ->
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { resume.fraction },
                    color = accent,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50))
                )
            }

            Spacer(Modifier.height(14.dp))
            DetailActions(state = state, accent = accent, onAction = onAction)

            state.tagline?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }

            state.overview?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(12.dp))
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }

            state.director?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(12.dp))
                LabeledText(label = "Director", value = it)
            }

            LabeledChips(label = "Genres", values = state.genres, accent = accent)
            LabeledChips(label = "Studios", values = state.studios, accent = accent)
            LabeledChips(label = "Collection", values = state.collections, accent = accent)

            ShowSeasonsSection(state = state, accent = accent, onAction = onAction)

            CastRail(cast = state.cast)
            CrewRail(crew = state.crew)

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LabeledText(label: String, value: String) {
    Row {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
