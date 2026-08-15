package zechs.zplex.feature_player

data class TrackOption(
    val id: Int,
    val name: String,
    val lang: String?,
    val selected: Boolean
)

/** Countdown card shown when an episode finishes and the next one is queued (H2). */
data class UpNextState(
    val title: String,
    val secondsLeft: Int
)

data class PlayerHudState(
    val title: String = "",
    val subtitle: String? = null,
    val controlsVisible: Boolean = true,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = true,
    val positionSec: Int = 0,
    val durationSec: Int = 0,
    val bufferedSec: Int = 0,
    val speed: Double = 1.0,
    val audioTracks: List<TrackOption> = emptyList(),
    val subTracks: List<TrackOption> = emptyList(),
    val hasNext: Boolean = false,
    val hasPrev: Boolean = false,
    val inPip: Boolean = false,
    val brightnessLevel: Float? = null,
    val volumeLevel: Float? = null,
    val seekPreviewSec: Int? = null,
    val error: String? = null,
    val upNext: UpNextState? = null
)

/** Callbacks the overlay invokes; the Activity owns the mpv/player side effects. */
class PlayerActions(
    val onToggleControls: () -> Unit = {},
    val onPlayPause: () -> Unit = {},
    val onSeekTo: (Int) -> Unit = {},
    val onSeekBy: (Int) -> Unit = {},
    val onSelectSpeed: (Double) -> Unit = {},
    val onSelectAudio: (Int) -> Unit = {},
    val onSelectSub: (Int) -> Unit = {},
    val onCycleScale: () -> Unit = {},
    val onEnterPip: () -> Unit = {},
    val onBack: () -> Unit = {},
    val onNext: () -> Unit = {},
    val onPrev: () -> Unit = {},
    val onBrightnessDelta: (Float) -> Unit = {},
    val onVolumeDelta: (Float) -> Unit = {},
    val onCancelUpNext: () -> Unit = {},
    val onPlayUpNextNow: () -> Unit = {},
    val onDismissError: () -> Unit = {}
)
