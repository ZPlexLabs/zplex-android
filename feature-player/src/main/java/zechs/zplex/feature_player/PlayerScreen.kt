package zechs.zplex.feature_player

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SPEEDS = listOf(0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0)

@Composable
fun PlayerOverlay(
    state: PlayerHudState,
    actions: PlayerActions,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {

        // Vertical-drag layer: left half = brightness, right half = volume.
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    var leftSide = true
                    detectVerticalDragGestures(
                        onDragStart = { offset -> leftSide = offset.x < size.width / 2f },
                        onVerticalDrag = { change, dragAmount ->
                            val delta = -dragAmount / size.height
                            if (leftSide) actions.onBrightnessDelta(delta)
                            else actions.onVolumeDelta(delta)
                            change.consume()
                        }
                    )
                }
        )

        // Tap layer: single tap toggles controls, double tap seeks ±10s by side.
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { actions.onToggleControls() },
                        onDoubleTap = { offset ->
                            if (offset.x < size.width / 2f) actions.onSeekBy(-10)
                            else actions.onSeekBy(10)
                        }
                    )
                }
        )

        if (state.isBuffering && state.error == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        GestureIndicator(state, Modifier.align(Alignment.Center))

        if (state.controlsVisible && !state.inPip && state.error == null && state.upNext == null) {
            Controls(state, actions)
        }

        state.upNext?.let { UpNextCard(it, actions, Modifier.align(Alignment.BottomEnd)) }

        state.error?.let { ErrorOverlay(it, actions, Modifier.align(Alignment.Center)) }
    }
}

@Composable
private fun Controls(state: PlayerHudState, actions: PlayerActions) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
    ) {
        // Top bar
        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = actions.onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    state.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                state.subtitle?.let {
                    Text(it, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                }
            }
            IconButton(onClick = actions.onEnterPip) {
                Icon(Icons.Filled.PictureInPictureAlt, "Picture in picture", tint = Color.White)
            }
        }

        // Center transport
        Row(
            Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.hasPrev) {
                IconButton(onClick = actions.onPrev) {
                    Icon(Icons.Filled.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }
            IconButton(onClick = { actions.onSeekBy(-10) }) {
                Icon(Icons.Filled.Replay10, "Rewind", tint = Color.White, modifier = Modifier.size(40.dp))
            }
            IconButton(onClick = actions.onPlayPause, modifier = Modifier.size(64.dp)) {
                Icon(
                    if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
            IconButton(onClick = { actions.onSeekBy(10) }) {
                Icon(Icons.Filled.Forward10, "Forward", tint = Color.White, modifier = Modifier.size(40.dp))
            }
            if (state.hasNext) {
                IconButton(onClick = actions.onNext) {
                    Icon(Icons.Filled.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }
        }

        // Bottom bar
        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatTime(state.positionSec), color = Color.White, fontSize = 12.sp)
                Slider(
                    value = state.positionSec.toFloat(),
                    onValueChange = { actions.onSeekTo(it.toInt()) },
                    valueRange = 0f..state.durationSec.coerceAtLeast(1).toFloat(),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Text(formatTime(state.durationSec), color = Color.White, fontSize = 12.sp)
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SpeedButton(state.speed, actions.onSelectSpeed)
                TrackButton(
                    icon = Icons.Filled.GraphicEq,
                    label = "Audio",
                    tracks = state.audioTracks,
                    onSelect = actions.onSelectAudio
                )
                TrackButton(
                    icon = Icons.Filled.ClosedCaption,
                    label = "Subtitles",
                    tracks = state.subTracks,
                    onSelect = actions.onSelectSub
                )
                IconButton(onClick = actions.onCycleScale) {
                    Icon(Icons.Filled.AspectRatio, "Resize", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SpeedButton(current: Double, onSelect: (Double) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.Speed, "Speed", tint = Color.White)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SPEEDS.forEach { speed ->
                DropdownMenuItem(
                    text = { Text("${speed}x${if (speed == current) "  ✓" else ""}") },
                    onClick = { onSelect(speed); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun TrackButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tracks: List<TrackOption>,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, enabled = tracks.isNotEmpty()) {
            Icon(icon, label, tint = Color.White)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            tracks.forEach { track ->
                DropdownMenuItem(
                    text = { Text("${track.name}${if (track.selected) "  ✓" else ""}") },
                    onClick = { onSelect(track.id); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun GestureIndicator(state: PlayerHudState, modifier: Modifier) {
    val text = when {
        state.seekPreviewSec != null -> formatTime(state.seekPreviewSec)
        state.brightnessLevel != null -> "Brightness ${(state.brightnessLevel * 100).toInt()}%"
        state.volumeLevel != null -> "Volume ${(state.volumeLevel * 100).toInt()}%"
        else -> null
    } ?: return
    Box(
        modifier
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = Color.White)
    }
}

@Composable
private fun UpNextCard(upNext: UpNextState, actions: PlayerActions, modifier: Modifier) {
    Box(
        modifier
            .padding(24.dp)
            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text("Up next in ${upNext.secondsLeft}s", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Text(upNext.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Row {
                TextButton(onClick = actions.onCancelUpNext) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = actions.onPlayUpNextNow) { Text("Play now") }
            }
        }
    }
}

@Composable
private fun ErrorOverlay(message: String, actions: PlayerActions, modifier: Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(32.dp)
            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Playback error",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(message, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = actions.onDismissError) { Text("Close") }
        }
    }
}

private fun formatTime(totalSec: Int): String {
    val s = totalSec.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
}
