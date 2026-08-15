package zechs.zplex.feature_player

import android.app.PictureInPictureParams
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zechs.mpv.MPVLib
import zechs.mpv.MPVLib.mpvEventId.MPV_EVENT_END_FILE
import zechs.mpv.MPVLib.mpvEventId.MPV_EVENT_FILE_LOADED
import zechs.mpv.MPVLib.mpvEventId.MPV_EVENT_PLAYBACK_RESTART
import zechs.mpv.MPVView
import zechs.mpv.utils.Utils
import zechs.zplex.common.player.PlayerArgs
import zechs.zplex.common.player.PlayerItem

@AndroidEntryPoint
class PlayerActivity : ComponentActivity(), MPVLib.EventObserver {

    private val viewModel by viewModels<PlayerViewModel>()

    private lateinit var player: MPVView
    private var playerReady = false

    private lateinit var args: PlayerArgs
    private var currentIndex = 0
    private val currentItem: PlayerItem get() = args.items[currentIndex]

    private var reachedRestart = false
    private var durationSec = 0

    private var indicatorJob: Job? = null
    private var heartbeatJob: Job? = null
    private var upNextJob: Job? = null
    private var markedPlayed = false

    private var hudState = mutableStateOf(PlayerHudState())
    private fun setHud(update: PlayerHudState.() -> PlayerHudState) {
        hudState.value = hudState.value.update()
    }

    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val parsed = readArgs()
        if (parsed == null || parsed.items.isEmpty()) {
            finish()
            return
        }
        args = parsed
        currentIndex = parsed.startIndex.coerceIn(0, parsed.items.lastIndex)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Utils.copyAssets(this)

        setContent {
            val state by hudState
            androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().background(Color.Black)) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val view = LayoutInflater.from(ctx)
                            .inflate(R.layout.view_mpv, null) as FrameLayout
                        player = view.findViewById(R.id.player)
                        player.initialize(filesDir.path, cacheDir.path)
                        player.addObserver(this@PlayerActivity)
                        playerReady = true
                        loadItem(currentIndex, resume = true)
                        view
                    }
                )
                PlayerOverlay(state, buildActions())
            }
        }
    }

    private fun readArgs(): PlayerArgs? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getSerializableExtra(PlayerArgs.EXTRA, PlayerArgs::class.java)
    } else {
        @Suppress("DEPRECATION")
        intent.getSerializableExtra(PlayerArgs.EXTRA) as? PlayerArgs
    }

    private fun loadItem(index: Int, resume: Boolean) {
        sendProgress()
        upNextJob?.cancel()
        currentIndex = index
        reachedRestart = false
        markedPlayed = false
        val item = args.items[index]
        setHud {
            copy(
                title = item.title,
                subtitle = item.subtitle,
                isBuffering = true,
                positionSec = 0,
                durationSec = 0,
                bufferedSec = 0,
                hasNext = index < args.items.lastIndex,
                hasPrev = index > 0,
                error = null,
                upNext = null
            )
        }
        lifecycleScope.launch {
            when (val result = viewModel.resolveStream(item.fileId)) {
                is StreamResult.Ready -> {
                    if (!result.isLocal) {
                        MPVLib.setOptionString(
                            "http-header-fields",
                            "Authorization: Bearer ${result.grant}"
                        )
                    }
                    if (resume && args.startPositionMs > 0) {
                        MPVLib.setOptionString("start", (args.startPositionMs / 1000).toString())
                    } else {
                        MPVLib.setOptionString("start", "0")
                    }
                    player.play(result.url)
                }

                is StreamResult.Failed -> setHud { copy(isBuffering = false, error = result.message) }
            }
        }
    }

    private fun buildActions() = PlayerActions(
        onToggleControls = { setHud { copy(controlsVisible = !controlsVisible) } },
        onPlayPause = { if (playerReady) player.cyclePause() },
        onSeekTo = { sec -> if (playerReady) player.timePos = sec },
        onSeekBy = { delta ->
            if (playerReady) {
                val pos = (player.timePos ?: 0) + delta
                player.timePos = pos.coerceIn(0, durationSec)
                showIndicator { copy(seekPreviewSec = pos.coerceIn(0, durationSec)) }
            }
        },
        onSelectSpeed = { speed ->
            MPVLib.setPropertyDouble("speed", speed)
            setHud { copy(speed = speed) }
        },
        onSelectAudio = { id -> selectTrack(audio = true, id = id) },
        onSelectSub = { id -> selectTrack(audio = false, id = id) },
        onCycleScale = { if (playerReady) player.cycleScale() },
        onEnterPip = { enterPip() },
        onBack = { finish() },
        onNext = { if (currentIndex < args.items.lastIndex) loadItem(currentIndex + 1, resume = false) },
        onPrev = { if (currentIndex > 0) loadItem(currentIndex - 1, resume = false) },
        onBrightnessDelta = { delta -> adjustBrightness(delta) },
        onVolumeDelta = { delta -> adjustVolume(delta) },
        onCancelUpNext = { cancelUpNext() },
        onPlayUpNextNow = { playUpNextNow() },
        onDismissError = { finish() }
    )

    // ---- Tracks ----

    private fun selectTrack(audio: Boolean, id: Int) {
        if (!playerReady) return
        if (audio) player.aid = id else player.sid = id
        val tracks = readTracks()
        setHud { copy(audioTracks = tracks.first, subTracks = tracks.second) }
        val item = currentItem
        if (item.isTv && item.tmdbId > 0) {
            val (audioLang, subLang) = tracksSelection()
            lifecycleScope.launch { viewModel.saveTrackPrefs(item.tmdbId, audioLang, subLang) }
        }
    }

    private fun tracksSelection(): Pair<String?, String?> {
        val (audio, sub) = readTracks()
        val audioLang = audio.firstOrNull { it.selected }?.lang
        val subLang = sub.firstOrNull { it.selected }?.lang
        return audioLang to subLang
    }

    private fun readTracks(): Pair<List<TrackOption>, List<TrackOption>> {
        val audio = mutableListOf<TrackOption>()
        val sub = mutableListOf(TrackOption(-1, "Off", null, player.sid == -1))
        val count = MPVLib.getPropertyInt("track-list/count") ?: 0
        for (i in 0 until count) {
            val type = MPVLib.getPropertyString("track-list/$i/type") ?: continue
            if (type != "audio" && type != "sub") continue
            val mpvId = MPVLib.getPropertyInt("track-list/$i/id") ?: continue
            val lang = MPVLib.getPropertyString("track-list/$i/lang")
            val title = MPVLib.getPropertyString("track-list/$i/title")
            val name = listOfNotNull(title, lang).joinToString(" · ").ifEmpty { "Track $mpvId" }
            val option = TrackOption(
                id = mpvId,
                name = name,
                lang = lang,
                selected = if (type == "audio") player.aid == mpvId else player.sid == mpvId
            )
            if (type == "audio") audio.add(option) else sub.add(option)
        }
        return audio to sub
    }

    private fun applyTrackPrefs() {
        val item = currentItem
        if (!item.isTv || item.tmdbId <= 0) return
        lifecycleScope.launch {
            val prefAudio = viewModel.preferredAudioLang(item.tmdbId)
            val prefSub = viewModel.preferredSubLang(item.tmdbId)
            val (audio, sub) = readTracks()
            prefAudio?.let { lang -> audio.firstOrNull { it.lang == lang }?.let { player.aid = it.id } }
            prefSub?.let { lang -> sub.firstOrNull { it.lang == lang }?.let { player.sid = it.id } }
            val refreshed = readTracks()
            setHud { copy(audioTracks = refreshed.first, subTracks = refreshed.second) }
        }
    }

    // ---- Gestures ----

    private fun adjustBrightness(delta: Float) {
        val lp = window.attributes
        val current = if (lp.screenBrightness in 0f..1f) lp.screenBrightness else 0.5f
        val next = (current + delta).coerceIn(0f, 1f)
        lp.screenBrightness = next
        window.attributes = lp
        showIndicator { copy(brightnessLevel = next) }
    }

    private fun adjustVolume(delta: Float) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val next = (current + (delta * max)).toInt().coerceIn(0, max)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, next, 0)
        showIndicator { copy(volumeLevel = next.toFloat() / max) }
    }

    private fun showIndicator(update: PlayerHudState.() -> PlayerHudState) {
        setHud(update)
        indicatorJob?.cancel()
        indicatorJob = lifecycleScope.launch {
            delay(900)
            setHud { copy(brightnessLevel = null, volumeLevel = null, seekPreviewSec = null) }
        }
    }

    // ---- Picture in picture ----

    private fun enterPip() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        runCatching { enterPictureInPictureMode(pipParams()) }
    }

    private fun pipParams(): PictureInPictureParams {
        val builder = PictureInPictureParams.Builder()
        val aspect = player.getVideoAspect()
        if (aspect != null && aspect > 0.0) {
            val num = (aspect * 1000).toInt().coerceIn(100, 23900)
            builder.setAspectRatio(Rational(num, 1000))
        }
        return builder.build()
    }

    override fun onUserLeaveHint() {
        if (hudState.value.isPlaying) enterPip()
        super.onUserLeaveHint()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        setHud { copy(inPip = isInPictureInPictureMode) }
    }

    // ---- MPV events ----

    override fun eventProperty(property: String) {
        if (property == "track-list") runOnUiThread {
            val (audio, sub) = readTracks()
            setHud { copy(audioTracks = audio, subTracks = sub) }
        }
    }

    override fun eventProperty(property: String, value: Long) = runOnUiThread {
        when (property) {
            "time-pos" -> {
                setHud { copy(positionSec = value.toInt()) }
                maybeMarkPlayed(value.toInt())
            }
            "duration" -> { durationSec = value.toInt(); setHud { copy(durationSec = value.toInt()) } }
            "demuxer-cache-time" -> setHud { copy(bufferedSec = value.toInt()) }
        }
    }

    override fun eventProperty(property: String, value: Boolean) = runOnUiThread {
        if (property == "pause") {
            setHud { copy(isPlaying = !value) }
            if (value) sendProgress() else startHeartbeat()
        }
    }

    override fun eventProperty(property: String, value: String) {}

    override fun eventProperty(property: String, value: Double) {}

    override fun event(eventId: Int) = runOnUiThread {
        when (eventId) {
            MPV_EVENT_FILE_LOADED -> {
                applyTrackPrefs()
            }

            MPV_EVENT_PLAYBACK_RESTART -> {
                reachedRestart = true
                setHud { copy(isBuffering = false) }
                startHeartbeat()
            }

            MPV_EVENT_END_FILE -> {
                if (!reachedRestart) {
                    setHud {
                        copy(
                            isBuffering = false,
                            error = "This file's format isn't supported on this device."
                        )
                    }
                } else {
                    onPlaybackEnded()
                }
            }
        }
    }

    private fun onPlaybackEnded() {
        markPlayedNow()
        sendProgress()
        heartbeatJob?.cancel()
        if (currentIndex < args.items.lastIndex) {
            startUpNextCountdown()
        }
    }

    // ---- Progress / played ----

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = lifecycleScope.launch {
            while (true) {
                delay(10_000)
                sendProgress()
            }
        }
    }

    private fun sendProgress() {
        if (!playerReady || durationSec <= 0) return
        val item = currentItem
        val pos = (player.timePos ?: return).toLong() * 1000
        val dur = durationSec.toLong() * 1000
        lifecycleScope.launch { viewModel.updateProgress(item, pos, dur) }
    }

    private fun maybeMarkPlayed(positionSec: Int) {
        if (markedPlayed || durationSec <= 0) return
        if (positionSec.toFloat() / durationSec >= 0.90f) markPlayedNow()
    }

    private fun markPlayedNow() {
        if (markedPlayed) return
        markedPlayed = true
        val item = currentItem
        lifecycleScope.launch { viewModel.markPlayed(item) }
    }

    // ---- Up next ----

    private fun startUpNextCountdown() {
        val next = args.items.getOrNull(currentIndex + 1) ?: return
        upNextJob?.cancel()
        upNextJob = lifecycleScope.launch {
            for (remaining in UP_NEXT_SECONDS downTo 1) {
                setHud { copy(upNext = UpNextState(next.title, remaining)) }
                delay(1_000)
            }
            playUpNextNow()
        }
    }

    private fun cancelUpNext() {
        upNextJob?.cancel()
        setHud { copy(upNext = null) }
    }

    private fun playUpNextNow() {
        upNextJob?.cancel()
        if (currentIndex < args.items.lastIndex) loadItem(currentIndex + 1, resume = false)
    }

    override fun onStop() {
        sendProgress()
        heartbeatJob?.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        sendProgress()
        heartbeatJob?.cancel()
        upNextJob?.cancel()
        if (playerReady) {
            player.removeObserver(this)
            player.destroy()
        }
        super.onDestroy()
    }

    companion object {
        private const val UP_NEXT_SECONDS = 10

        fun newIntent(context: Context, args: PlayerArgs) =
            android.content.Intent(context, PlayerActivity::class.java)
                .putExtra(PlayerArgs.EXTRA, args)
    }
}
