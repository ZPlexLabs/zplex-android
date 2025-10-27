package zechs.zplex.ui.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.media.AudioManager.AUDIOFOCUS_LOSS
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
import android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.AudioManager.STREAM_MUSIC
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import android.view.animation.AccelerateInterpolator
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media.session.MediaButtonReceiver
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.samsung.android.sdk.penremote.ButtonEvent
import com.samsung.android.sdk.penremote.SpenEventListener
import com.samsung.android.sdk.penremote.SpenUnit
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zechs.mpv.MPVLib
import zechs.mpv.MPVLib.mpvEventId.MPV_EVENT_END_FILE
import zechs.mpv.MPVLib.mpvEventId.MPV_EVENT_FILE_LOADED
import zechs.mpv.MPVLib.mpvEventId.MPV_EVENT_PLAYBACK_RESTART
import zechs.mpv.MPVView
import zechs.mpv.utils.Utils
import zechs.zplex.R
import zechs.zplex.data.model.PosterSize
import zechs.zplex.databinding.ActivityMpvBinding
import zechs.zplex.databinding.PlayerControlViewBinding
import zechs.zplex.databinding.SideSheetEpisodesBinding
import zechs.zplex.ui.player.sidesheet.episodes.adapter.SideSheetEpisodesAdapter
import zechs.zplex.utils.Constants.DRIVE_API
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.SpenRemoteHelper
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.util.Orientation
import zechs.zplex.utils.util.getNextOrientation
import zechs.zplex.utils.util.setOrientation
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

@AndroidEntryPoint
class MPVActivity : AppCompatActivity(), MPVLib.EventObserver {

    companion object {
        const val TAG = "MPVActivity"

        // Notification channel constants
        private const val PLAYER_CHANNEL_ID = "zplex_video_player"
        private const val MEDIA_SESSION_ID = "zplex_session"
        private const val PLAYER_NOTIFICATION_ID = 100002

        // fraction to which audio volume is ducked on loss of audio focus
        private const val AUDIO_FOCUS_DUCKING = 0.5f
        private const val SKIP_DURATION = 10 // in seconds
    }

    // Media session & notification
    private lateinit var mediaSession: MediaSessionCompat
    private var notificationManager: NotificationManager? = null
    private var currentNotification: Notification? = null
    private var isSessionActive = false

    private lateinit var audioManager: AudioManager
    private var audioFocusRestore: () -> Unit = {}

    /**
     * DO NOT USE THIS
     */
    private var activityIsStopped = false

    // View-binding
    private lateinit var binding: ActivityMpvBinding
    private lateinit var player: MPVView
    private lateinit var controller: PlayerControlViewBinding

    private val viewModel by viewModels<PlayerViewModel>()

    // States
    private var activityIsForeground = true
    private var userIsOperatingSeekbar = false
    private var controlsLocked = false

    // Configs
    private var onLoadCommands = mutableListOf<Array<String>>()
    private val speeds = arrayOf(0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0)
    private var orientation = Orientation.LANDSCAPE
    private lateinit var sideSheetDialog: SideSheetDialog
    private val episodesSheet by lazy {
        SideSheetEpisodesBinding.inflate(layoutInflater)
    }
    private val sideSheetEpisodeAdapter by lazy {
        SideSheetEpisodesAdapter(
            episodeOnClick = { episode ->
                viewModel.play(episode.fileId!!)
                sideSheetDialog.hide()
            }
        )
    }

    private val spenListener = SpenEventListener { spenEvent ->
        val buttonEvent = ButtonEvent(spenEvent)
        when (buttonEvent.action) {
            ButtonEvent.ACTION_UP -> {
                player.cyclePause()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        Utils.copyAssets(this)

        binding = ActivityMpvBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        player = binding.player
        controller = binding.controller
        sideSheetDialog = SideSheetDialog(this)

        controller
            .playerToolbar
            .setNavigationOnClickListener { finish() }

        controller
            .playerToolbar
            .setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_playlist -> {
                        showPlaylistDialog()
                        return@setOnMenuItemClickListener true
                    }
                }
                return@setOnMenuItemClickListener false
            }

        sideSheetDialog.setContentView(episodesSheet.root)
        episodesSheet.rvList.apply {
            adapter = sideSheetEpisodeAdapter
            layoutManager = LinearLayoutManager(
                applicationContext, LinearLayoutManager.VERTICAL, false
            )
        }
        player.initialize(filesDir.path, cacheDir.path)
        player.addObserver(this)

        audioManager = getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager

        @Suppress("DEPRECATION")
        audioManager.requestAudioFocus(
            audioFocusChangeListener,
            STREAM_MUSIC,
            AUDIOFOCUS_GAIN
        ).also {
            if (it != AUDIOFOCUS_REQUEST_GRANTED) {
                Log.w(TAG, "Audio focus not granted")
                onLoadCommands.add(arrayOf("set", "pause", "yes"))
            }
        }

        // setVolumeControlStream
        volumeControlStream = STREAM_MUSIC


        controller.apply {
            // progress bar
            progressBar.setOnSeekBarChangeListener(seekBarChangeListener)

            // init onClick listeners
            btnPlayPause.setOnClickListener { player.cyclePause() }
            btnPip.setOnClickListener { goIntoPiP() }
            exoFfwd.setOnClickListener { skipForward() }
            exoRew.setOnClickListener { rewindBackward() }
            btnAudio.setOnClickListener { pickAudio() }
            btnSubtitle.setOnClickListener { pickSub() }
            btnChapter.setOnClickListener { pickChapter() }
            btnSpeed.setOnClickListener { pickSpeed() }
            btnResize.setOnClickListener { player.cycleScale() }
            btnNext.setOnClickListener {
                saveProgress(viewModel.head)
                player.stop()
                viewModel.next()
            }
            btnPrevious.setOnClickListener {
                saveProgress(viewModel.head)
                player.stop()
                viewModel.previous()
            }

            btnRotate.setOnClickListener {
                orientation = getNextOrientation(orientation)
                Log.d(TAG, "orientation=${orientation}")
                setOrientation(this@MPVActivity, orientation)
            }

            btnLock.setOnClickListener {
                controlsLocked = true
                handleLockingControls()
            }

            btnUnlock.setOnClickListener {
                controlsLocked = false
                handleLockingControls()
            }

        }

        // hide/show controller
        player.setOnClickListener {
            val root = controller.root
            TransitionManager.beginDelayedTransition(root, Fade())
            root.isVisible = !root.isVisible
            if (root.isVisible) {
                handleLockingControls()
            }
        }

        updateOrientation(resources.configuration)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.startDuration.collect { startPosition ->
                    resumeVideo(startPosition)
                }
            }
        }
        playlistObserver()
        playMedia()
        initMediaSession()

        if (isSamsungWithSPen()) {
            SpenRemoteHelper.initialize(
                context = this,
                onConnected = {
                    SpenRemoteHelper.registerListener(SpenUnit.TYPE_BUTTON, spenListener)
                },
                onDisconnected = { errorCode ->
                    SpenRemoteHelper.unregisterListener(SpenUnit.TYPE_BUTTON)
                    Log.e("SpenRemote", "Disconnected with code: $errorCode")
                }
            )
        }
        addOnPictureInPictureModeChangedListener { info ->
            onPiPModeChangedImpl(info.isInPictureInPictureMode)
        }
    }

    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (!fromUser || controlsLocked)
                return
            player.timePos = progress
            updatePlaybackPos(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            userIsOperatingSeekbar = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            userIsOperatingSeekbar = false
        }
    }
    private val audioFocusChangeListener = OnAudioFocusChangeListener { type ->
        Log.v(TAG, "Audio focus changed: $type")
        when (type) {
            AUDIOFOCUS_LOSS,
            AUDIOFOCUS_LOSS_TRANSIENT -> {
                // loss can occur in addition to ducking, so remember the old callback
                val oldRestore = audioFocusRestore
                val wasPlayerPaused = player.paused ?: false
                player.paused = true
                audioFocusRestore = {
                    oldRestore()
                    if (!wasPlayerPaused) player.paused = false
                }
            }

            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                MPVLib.command(arrayOf("multiply", "volume", AUDIO_FOCUS_DUCKING.toString()))
                audioFocusRestore = {
                    val inv = 1f / AUDIO_FOCUS_DUCKING
                    MPVLib.command(arrayOf("multiply", "volume", inv.toString()))
                }
            }

            AUDIOFOCUS_GAIN -> {
                audioFocusRestore()
                audioFocusRestore = {}
            }
        }
    }


    private fun playMedia() {
        val playlist = intent.getStringExtra("playlist")
        val startIndex = intent.getIntExtra("startIndex", 0)
        if (playlist == null) {
            Log.d(TAG, "Playlist is required. exiting...")
            finish()
            return
        }
        viewModel.setPlaylist(playlist, startIndex)
        sideSheetEpisodeAdapter.submitList(viewModel.getPlaylist())
    }

    private fun playlistObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.current.collect {
                    handlePlaylistStates(it)
                }
            }
        }
    }

    private fun handlePlaylistStates(resource: Resource<PlayerViewModel.Playback?>) {
        when (resource) {
            is Resource.Loading -> {
                // ignore
            }

            is Resource.Error -> {
                showErrorDialog(resource.message ?: getString(R.string.unknown_error))
                MPVLib.command(arrayOf("stop"))
            }

            is Resource.Success -> {
                val accessToken = resource.data!!.token
                val playbackItem = resource.data.item
                if (playbackItem != null) {
                    val playlistButton = binding.controller.playerToolbar.menu
                        .findItem(R.id.action_playlist)
                    controller.playerToolbar.title = playbackItem.title
                    if (playbackItem is Show) {
                        val subtextBuilder = StringBuilder()
                        subtextBuilder.append(
                            "S%02dE%02d".format(
                                playbackItem.seasonNumber,
                                playbackItem.episodeNumber
                            )
                        )
                        if (playbackItem.episodeTitle != null) {
                            subtextBuilder.append(" - ${playbackItem.episodeTitle}")
                        }
                        controller.playerToolbar.subtitle = subtextBuilder.toString()
                        viewModel.getWatch(
                            playbackItem.tmdbId,
                            true,
                            playbackItem.seasonNumber,
                            playbackItem.episodeNumber
                        )
                        playlistButton.apply {
                            isVisible = true
                            playlistButton.setOnMenuItemClickListener {
                                showPlaylistDialog()
                                return@setOnMenuItemClickListener true
                            }
                        }
                    } else {
                        playlistButton.isVisible = false
                        viewModel.getWatch(playbackItem.tmdbId, false, null, null)
                    }
                    val playUri = if (playbackItem.offline) {
                        Uri.fromFile(File(playbackItem.fileId)).toString()
                    } else getStreamUrl(playbackItem.fileId)

                    Log.d(TAG, "playUri: $playUri")

                    if (!playbackItem.offline) {
                        MPVLib.setOptionString(
                            "http-header-fields",
                            "Authorization: Bearer $accessToken"
                        )
                    }
                    if (player.vo != null && player.vo!!) {
                        MPVLib.command(arrayOf("loadfile", playUri))
                    }
                    player.play(playUri)
                }
                TransitionManager.endTransitions(controller.mainControls)
                TransitionManager.beginDelayedTransition(
                    controller.mainControls,
                    MaterialFadeThrough().apply {
                        interpolator = AccelerateInterpolator()
                        duration = 150L
                    }
                )
                val isNext = playbackItem?.next != null
                val isPrev = playbackItem?.prev != null
                controller.btnNext.isInvisible = !isNext
                controller.btnPrevious.isInvisible = !isPrev
            }
        }
    }

    private fun showPlaylistDialog() {
        val currentIndex = viewModel.findIndexFromFileId(viewModel.head!!.fileId)
        episodesSheet.rvList.smoothScrollToPosition(currentIndex)
        sideSheetDialog.show()
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.something_went_wrong))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }.show()
    }

    private fun resumeVideo(startPosition: Long) {
        val startPositionInSeconds = startPosition / 1000
        Log.d(TAG, "MPV(resumeVideo=${Utils.prettyTime(startPositionInSeconds.toInt())}) startPositionInSeconds=$startPositionInSeconds")
        MPVLib.setOptionString("start", Utils.prettyTime(startPositionInSeconds.toInt()))
        val timePos = MPVLib.getPropertyInt("time-pos")
        if ((timePos != null) && (abs(timePos - startPositionInSeconds) > 2)) {
            Log.d(TAG, "MPV(resumeVideo=${startPositionInSeconds.toInt()}) timePos=$timePos")
            MPVLib.command(arrayOf("seek", startPositionInSeconds.toString(), "absolute"))
        }
    }

    private fun getStreamUrl(fileId: String): String {
        val uri = "${DRIVE_API}/files/${fileId}?supportsAllDrives=True&alt=media".toUri()
        Log.d(TAG, "STREAM_URL=$uri")
        return uri.toString()
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun updatePlaybackPos(position: Int) {
        controller.postion.text = Utils.prettyTime(position)
        if (!userIsOperatingSeekbar) {
            controller.progressBar.progress = position
        }
    }

    private fun updatePlaybackDuration(duration: Int) {
        controller.duration.text = Utils.prettyTime(duration)
        if (!userIsOperatingSeekbar) {
            controller.progressBar.max = duration
        }
    }

    private enum class PlaybackState {
        PLAYING,
        PAUSED,
        BUFFERING
    }

    private fun updatePlaybackStatus(state: PlaybackState) {
        Log.d(TAG, "updatePlaybackStatus($state)")
        TransitionManager.endTransitions(controller.mainControls)
        TransitionManager.beginDelayedTransition(
            controller.mainControls,
            AutoTransition().apply { duration = 250L }
        )

        if (state == PlaybackState.BUFFERING) {
            binding.buffering.isInvisible = false
            controller.btnPlayPause.isInvisible = true
        } else {
            binding.buffering.isInvisible = true
            controller.btnPlayPause.isInvisible = false
            controller.btnPlayPause.icon = ContextCompat.getDrawable(
                /* context */ applicationContext,
                /* drawableId */ if (state == PlaybackState.PAUSED) R.drawable.ic_play_24
                else R.drawable.ic_pause_24
            )
        }
        if (state == PlaybackState.BUFFERING) {
            window.clearFlags(FLAG_KEEP_SCREEN_ON)
        } else {
            window.addFlags(FLAG_KEEP_SCREEN_ON)
        }
        updateNotification()
    }

    private fun skipForward() {
        val currentPos = player.timePos ?: return
        val newPos = currentPos + SKIP_DURATION
        player.timePos = newPos
    }

    private fun rewindBackward() {
        val currentPos = player.timePos ?: return
        val newPos = currentPos - SKIP_DURATION
        player.timePos = newPos
    }


    data class TrackData(
        val trackId: Int,
        val trackType: String
    )

    private fun trackSwitchNotification(f: () -> TrackData) {
        val (trackId, trackType) = f()
        val trackPrefix = when (trackType) {
            "audio" -> getString(R.string.audio)
            "sub" -> getString(R.string.subtitles)
            "video" -> getString(R.string.video)
            else -> "???"
        }

        if (trackId == -1) {
            configSnackbar("$trackPrefix ${getString(R.string.track_off)}")
            return
        }

        val trackName = player.tracks[trackType]
            ?.firstOrNull { it.mpvId == trackId }
            ?.name
            ?: "???"

        configSnackbar("$trackPrefix $trackName")
    }

    private fun pickAudio() {
        selectTrack(
            title = getString(R.string.select_audio),
            type = "audio",
            get = { player.aid },
            set = { player.aid = it }
        )
    }

    private fun pickSub() {
        selectTrack(
            title = getString(R.string.select_subtitle),
            type = "sub",
            get = { player.sid },
            set = { player.sid = it }
        )
    }

    private fun pickChapter() {
        val chapters = player.loadChapters()

        if (chapters.isEmpty()) {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.chapters))
                .setItems(arrayOf("None")) { dialog, _ ->
                    dialog.dismiss()
                }.show()
            return
        }

        val chapterArray = chapters.map {
            val timeCode = Utils.prettyTime(it.time.roundToInt())
            if (!it.title.isNullOrEmpty()) {
                getString(R.string.ui_chapter, it.title, timeCode)
            } else {
                getString(R.string.ui_chapter_fallback, it.index + 1, timeCode)
            }
        }.toTypedArray()

        val selectedIndex = MPVLib.getPropertyInt("chapter") ?: 0

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.chapters))
            .setSingleChoiceItems(chapterArray, selectedIndex) { dialog, item ->
                MPVLib.setPropertyInt("chapter", chapters[item].index)
                dialog.dismiss()
            }.show()

    }

    private fun selectTrack(
        title: String,
        type: String,
        get: () -> Int,
        set: (Int) -> Unit
    ) {
        val tracks = player.tracks.getValue(type)
        val selectedMpvId = get()
        val selectedIndex = tracks.indexOfFirst { it.mpvId == selectedMpvId }

        MaterialAlertDialogBuilder(this).apply {
            setTitle(title)
            setSingleChoiceItems(
                tracks.map { it.name }.toTypedArray(),
                selectedIndex
            ) { dialog, item ->
                val trackId = tracks[item].mpvId
                set(trackId)
                dialog.dismiss()
                trackSwitchNotification { TrackData(trackId, type) }
            }
        }.also { it.show() }
    }

    private fun configSnackbar(msg: String, duration: Int = 750) {
        Snackbar.make(
            controller.root, msg, duration
        ).apply {
            anchorView = controller.linearLayout2
        }.also { it.show() }
    }

    private fun pickSpeed() {
        val currentSpeed = MPVLib.getPropertyDouble("speed")
        val selectedIndex = speeds.toList().indexOf(currentSpeed)

        Log.d(TAG, "currentSpeed=$currentSpeed")

        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.select_speed))
            setSingleChoiceItems(
                speeds.map { it.toString() }.toTypedArray(),
                selectedIndex
            ) { dialog, item ->
                setSpeed(speeds[item])
                dialog.dismiss()
                configSnackbar("Playback speed set to ${speeds[item]}x")
            }
        }.also { it.show() }
    }

    private fun setSpeed(speed: Double) {
        MPVLib.setPropertyDouble("speed", speed)
    }


    private fun updateOrientation(newConfig: Configuration) {
        when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                controller.btnRotate.apply {
                    orientation = Orientation.PORTRAIT
                    tooltipText = getString(R.string.landscape)
                    icon = ContextCompat.getDrawable(
                        /* context */ this@MPVActivity,
                        /* drawableId */ R.drawable.ic_landscape_24
                    )
                }
            }

            else -> {
                controller.btnRotate.apply {
                    orientation = Orientation.LANDSCAPE
                    tooltipText = getString(R.string.portrait)
                    icon = ContextCompat.getDrawable(
                        /* context */ this@MPVActivity,
                        /* drawableId */ R.drawable.ic_portrait_24
                    )
                }
            }
        }
    }


    private fun handleLockingControls() {
        TransitionManager.beginDelayedTransition(
            controller.root,
            AutoTransition().apply { duration = 150L }
        )
        if (controlsLocked) {
            lockControls()
        } else {
            unlockControls()
        }
    }

    private fun lockControls() {
        controller.apply {
            controlsScrollView.visibility = View.GONE
            mainControls.visibility = View.GONE
            btnUnlock.visibility = View.VISIBLE
            playerToolbar.visibility = View.GONE
        }
    }

    private fun unlockControls() {
        controller.apply {
            btnUnlock.visibility = View.GONE
            playerToolbar.visibility = View.VISIBLE
            controlsScrollView.visibility = View.VISIBLE
            mainControls.visibility = View.VISIBLE
        }
    }

    private fun saveProgress(current: PlaybackItem?) {
        val watchedDuration = player.timePos ?: return
        val totalDuration = player.duration ?: return
        Log.d(TAG, "MPV(current=$watchedDuration, total=$totalDuration)")
        // convert these two values from seconds to milliseconds
        val watchedDurationInMills = (watchedDuration * 1000).toLong()
        val totalDurationInMills = (totalDuration * 1000).toLong()
        Log.d(TAG, "MPV(current=$watchedDurationInMills, total=$totalDurationInMills)")
        Log.d(TAG, "MPV(saveProgress=${Utils.prettyTime(watchedDuration)})")

        val watchProgress = (watchedDuration.toDouble() / totalDuration.toDouble()).toFloat() * 100
        val minWatchingThresholdPercent = 1
        if (watchProgress > minWatchingThresholdPercent) {
            viewModel.saveProgress(current, watchedDurationInMills, totalDurationInMills)
        }
    }

    private fun isSamsungWithSPen(): Boolean {
        val hasSPenFeature = this.packageManager.hasSystemFeature("com.sec.feature.spen_usp")
        return Build.MANUFACTURER.equals("samsung", ignoreCase = true) && hasSPenFeature
    }

    private fun initMediaSession() {
        val mediaButtonReceiver = ComponentName(
            applicationContext,
            NotificationButtonReceiver::class.java
        )

        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_ID, mediaButtonReceiver, null)
            .apply {
                setCallback(object : MediaSessionCompat.Callback() {
                    override fun onPlay() {
                        player.cyclePause()
                        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                        updateNotification()
                    }

                    override fun onPause() {
                        player.cyclePause()
                        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                        updateNotification()
                    }

                    override fun onSeekTo(pos: Long) {
                        player.timePos = (pos / 1000).toInt()
                    }

                    override fun onSkipToNext() {
                        saveProgress(viewModel.head)
                        player.stop()
                        viewModel.next()
                        updateNotification()
                    }

                    override fun onSkipToPrevious() {
                        saveProgress(viewModel.head)
                        player.stop()
                        viewModel.previous()
                        updateNotification()
                    }
                })
            }

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            PLAYER_CHANNEL_ID,
            "Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Playback controls"
        }
        notificationManager?.createNotificationChannel(channel)

        if (!isSessionActive) {
            mediaSession.isActive = true
            isSessionActive = true
        }
    }

    private fun releaseMediaSession() {
        if (isSessionActive) {
            mediaSession.isActive = false
            isSessionActive = false
        }
        try {
            mediaSession.release()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release Media Session", e)
        }
        notificationManager?.cancel(PLAYER_NOTIFICATION_ID)
    }

    private fun updatePlaybackState(
        state: Int,
        customActions: List<PlaybackStateCompat.CustomAction>? = null
    ) {
        if (!::mediaSession.isInitialized) return

        val positionMs = (player.timePos?.times(1000))?.toLong() ?: 0L
        val playbackSpeed = if (player.paused == true)
            0f else (MPVLib.getPropertyDouble("speed")?.toFloat() ?: 1f)

        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
            .setState(state, positionMs, playbackSpeed)

        customActions?.forEach { stateBuilder.addCustomAction(it) }

        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private fun updateMetadata(metadata: MediaMetadataData) {
        if (!::mediaSession.isInitialized) return

        val metadataBuilder = MediaMetadataCompat.Builder()
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadata.title)
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metadata.title)

        val subtitle = when (metadata) {
            is ShowMetadata -> {
                val epString = "S${metadata.seasonNumber} • E${metadata.episodeNumber}"
                val epName = metadata.episodeName?.let { " — $it" } ?: ""
                epString + epName
            }

            is MovieMetadata -> metadata.studio ?: ""
        }

        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, subtitle)
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, subtitle)

        val durationMs = metadata.durationSecs?.times(1000L) ?: ((player.duration ?: 0) * 1000L)
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)

        metadata.posterPath?.let { path ->
            val posterUrl = "$TMDB_IMAGE_PREFIX/${PosterSize.w342}${path}"
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val bitmap = Glide.with(this@MPVActivity)
                        .asBitmap()
                        .load(posterUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .submit()
                        .get()
                    metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load poster: ${e.message}")
                } finally {
                    mediaSession.setMetadata(metadataBuilder.build())
                }
            }
        } ?: mediaSession.setMetadata(metadataBuilder.build())
    }

    private fun buildNotification(): Notification {
        val controller = mediaSession.controller
        val mediaMetadata = controller.metadata

        val title = mediaMetadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            ?: controller.queueTitle?.toString() ?: "Preparing playback..."
        val subtitle =
            mediaMetadata?.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE) ?: ""

        val playAction = if (player.paused == true) {
            NotificationCompat.Action.Builder(
                R.drawable.ic_play_24,
                "Play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PLAY
                )
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                R.drawable.ic_pause_24,
                "Pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PAUSE
                )
            ).build()
        }

        val prevAction = NotificationCompat.Action.Builder(
            R.drawable.ic_previous_24,
            "Previous",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                this,
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
        ).build()
        val nextAction = NotificationCompat.Action.Builder(
            R.drawable.ic_next_24,
            "Next",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                this,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )
        ).build()

        val style = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        val builder = NotificationCompat.Builder(this, PLAYER_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSmallIcon(R.drawable.ic_tv_play)
            .setStyle(style)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(prevAction)
            .addAction(playAction)
            .addAction(nextAction)
            .setOnlyAlertOnce(true)

        return builder.build()
    }

    private fun updateNotification() {
        currentNotification = buildNotification()
        notificationManager?.notify(PLAYER_NOTIFICATION_ID, currentNotification)
    }

    private fun onPiPModeChangedImpl(state: Boolean) {
        Log.v(TAG, "onPiPModeChanged($state)")
        if (state) {
            binding.controller.root.isInvisible = true
            return
        }

        binding.controller.root.isInvisible = false
        if (player.paused != null) {
            updatePlaybackStatus(if (player.paused!!) PlaybackState.PAUSED else PlaybackState.PLAYING)
        }
        // For whatever stupid reason Android provides no good detection for when PiP is exited
        // so we have to do this shit <https://stackoverflow.com/questions/43174507/#answer-56127742>
        // If we don't exit the activity here it will stick around and not be retrievable from the
        // recents screen, or react to onNewIntent().
        if (activityIsStopped) {
            // Note: On Android 12 or older there's another bug with this: the result will not
            // be delivered to the calling activity and is instead instantly returned the next
            // time, which makes it looks like the file picker is broken.
            saveProgress(viewModel.head)
            player.stop()
            Log.d(TAG, "Killing MPV Player as exited from PiP Mode")
            finish()
        }
    }

    private fun goIntoPiP() {
        updatePiPParams(true)
        enterPictureInPictureMode()
    }

    /**
     * Update Picture-in-picture parameters. Will only run if in PiP mode unless
     * `force` is set.
     */
    private fun updatePiPParams(force: Boolean = false) {
        if (!isInPictureInPictureMode && !force)
            return

        val playPauseAction = if (player.paused != null && player.paused!!) {
            makeRemoteAction(R.drawable.ic_play_24, R.string.btn_play, "PLAY_PAUSE")
        } else {
            makeRemoteAction(R.drawable.ic_pause_24, R.string.btn_pause, "PLAY_PAUSE")
        }
        val actions = mutableListOf<RemoteAction>()
        actions.add(playPauseAction)

        val params = with(PictureInPictureParams.Builder()) {
            val aspect = player.getVideoAspect() ?: 0.0
            setAspectRatio(Rational(aspect.times(10000).toInt(), 10000))
            setAutoEnterEnabled(true)
            setSeamlessResizeEnabled(true)
            setActions(actions)
        }
        try {
            setPictureInPictureParams(params.build())
        } catch (e: IllegalArgumentException) {
            // Android has some limits of what the aspect ratio can be
            params.setAspectRatio(Rational(1, 1))
            setPictureInPictureParams(params.build())
        }
    }

    private fun makeRemoteAction(
        @DrawableRes icon: Int,
        @StringRes title: Int,
        intentAction: String
    ): RemoteAction {
        val intent = NotificationButtonReceiver.createIntent(this, intentAction)
        return RemoteAction(Icon.createWithResource(this, icon), getString(title), "", intent)
    }

    ////////////////    MPV EVENTS    ////////////////

    override fun eventProperty(property: String, value: Boolean) {
        if (activityIsForeground && property == "pause") {
            runOnUiThread { updatePlaybackStatus(if (value) PlaybackState.PAUSED else PlaybackState.PLAYING) }
        }
    }

    override fun eventProperty(property: String, value: Long) {
        if (!activityIsForeground) return
        runOnUiThread {
            when (property) {
                "time-pos" -> {
                    updatePlaybackPos(value.toInt())
                    val state =
                        if (player.paused == true) PlaybackStateCompat.STATE_PAUSED
                        else PlaybackStateCompat.STATE_PLAYING
                    updatePlaybackState(state)
                }

                "duration" -> {
                    updatePlaybackDuration(value.toInt())
                    mediaSession.controller.metadata?.let { currentMetadata ->
                        val updatedMetadata = MediaMetadataCompat.Builder(currentMetadata)
                            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, value * 1000L)
                            .build()
                        mediaSession.setMetadata(updatedMetadata)
                    }
                }

                "demuxer-cache-time" -> updateBufferedPos(value.toInt())
            }
            updateNotification()
        }
    }

    private fun updateBufferedPos(buffered: Int) {
        controller.progressBar.secondaryProgress = buffered
    }

    override fun eventProperty(property: String) {
        if (!activityIsForeground) return
        runOnUiThread { eventPropertyUi(property) }
    }

    override fun eventProperty(property: String, value: String) {
        if (!activityIsForeground) return
        runOnUiThread { eventPropertyUi(property) }
    }

    override fun eventProperty(property: String, value: Double) {
        runOnUiThread { eventPropertyUi(property) }
    }

    private fun eventPropertyUi(property: String) {
        if (activityIsForeground && property == "track-list") {
            player.loadTracks()
        }

        if (activityIsForeground && property == "core-idle") {
            runOnUiThread { updatePlaybackStatus(if (player.paused!!) PlaybackState.PAUSED else PlaybackState.PLAYING) }
        }
    }

    override fun event(eventId: Int) {
        if (activityIsForeground) {
            runOnUiThread {
                if (eventId == MPV_EVENT_PLAYBACK_RESTART) {
                    updatePlaybackStatus(if (player.paused!!) PlaybackState.PAUSED else PlaybackState.PLAYING)
                } else {
                    if (player.isBuffering != null) {
                        if (player.isBuffering!! && !player.paused!!) {
                            updatePlaybackStatus(PlaybackState.BUFFERING)
                        } else {
                            val pausedIcon = ContextCompat.getDrawable(
                                /* context */ applicationContext,
                                /* drawableId */ R.drawable.ic_pause_24
                            )
                            if (player.paused!! && controller.btnPlayPause.icon != pausedIcon) {
                                updatePlaybackStatus(PlaybackState.PAUSED)
                            } else if (!player.paused!! && controller.btnPlayPause.icon == pausedIcon) {
                                updatePlaybackStatus(PlaybackState.PLAYING)
                            }
                        }
                    }
                }
                if (eventId == MPV_EVENT_FILE_LOADED) {
                    Log.d(TAG, "File has starting playing...")

                    viewModel.head?.let { head ->
                        val metadata: MediaMetadataData? = when (head) {
                            is Movie -> {
                                MovieMetadata(
                                    title = head.title,
                                    posterPath = head.posterPath,
                                    durationSecs = player.duration ?: 0,
                                    studio = "No studio"
                                )
                            }

                            is Show -> {
                                ShowMetadata(
                                    title = head.title,
                                    posterPath = head.posterPath,
                                    durationSecs = player.duration ?: 0,
                                    seasonNumber = head.seasonNumber,
                                    episodeNumber = head.episodeNumber,
                                    episodeName = head.episodeTitle
                                )
                            }

                            else -> null
                        }

                        metadata?.let {
                            updateMetadata(it)
                            updatePlaybackState(
                                if (player.paused == true)
                                    PlaybackStateCompat.STATE_PAUSED
                                else
                                    PlaybackStateCompat.STATE_PLAYING
                            )
                            updateNotification()
                        }
                    }

                }
                if (eventId == MPV_EVENT_END_FILE) {
                    binding.controller.apply {
                        val progress = progressBar.progress
                        val max = binding.controller.progressBar.max
                        val completed = (max - progress) <= 2
                        Log.d(
                            TAG,
                            "End of file, completed=$completed, progress=$progress, max=$max"
                        )
                        if (completed && btnNext.isVisible) {
                            viewModel.next()
                            Log.d(TAG, "End of file, auto-playing next in playlist")
                        } else {
                            Log.d(TAG, "End of file, but last item or not completed")
                        }
                    }
                }
            }
        }
    }

    ////////////////    END OF MPV EVENTS    ////////////////


    override fun onPause() {
        if (isInMultiWindowMode || isInPictureInPictureMode) {
            Log.v(TAG, "Going into multi-window mode")
            super.onPause()
            return
        }
        player.cyclePause()
        saveProgress(viewModel.head)

        activityIsForeground = false

        if (isFinishing) {
            Log.v(TAG, "isFinishing: mpv stop")
            MPVLib.command(arrayOf("stop"))
        }
        super.onPause()
    }

    override fun onResume() {
        // If we weren't actually in the background
        // (e.g. multi window mode), don't reinitialize stuff
        if (activityIsForeground) {
            super.onResume()
            return
        }

        activityIsForeground = true
        super.onResume()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateOrientation(newConfig)
    }

    override fun onStart() {
        super.onStart()
        activityIsStopped = false
    }

    override fun onStop() {
        super.onStop()
        activityIsStopped = true
    }

    override fun onDestroy() {
        Log.v(TAG, "Exiting.")

        if (isSamsungWithSPen()) {
            SpenRemoteHelper.disconnect(this)
        }
        @Suppress("DEPRECATION")
        audioManager.abandonAudioFocus(audioFocusChangeListener)

        player.removeObserver(this)
        player.destroy()
        releaseMediaSession()

        super.onDestroy()
    }

}