package zechs.zplex.ui.activity.playerkt

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Icon
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.text.TextUtils
import android.util.Log
import android.util.Rational
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory
import com.google.android.exoplayer2.extractor.ts.TsExtractor
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.ui.TimeBar.OnScrubListener
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.material.snackbar.Snackbar
import zechs.zplex.R
import zechs.zplex.ui.activity.player.CustomDefaultTimeBar
import zechs.zplex.ui.activity.player.CustomDefaultTrackNameProvider
import zechs.zplex.ui.activity.player.CustomStyledPlayerView
import zechs.zplex.ui.activity.player.Utils
import zechs.zplex.ui.activity.player.Utils.Companion.adjustVolume
import zechs.zplex.ui.activity.player.Utils.Companion.deviceLanguages
import zechs.zplex.ui.activity.player.Utils.Companion.formatMillisSign
import zechs.zplex.ui.activity.player.Utils.Companion.getNextOrientation
import zechs.zplex.ui.activity.player.Utils.Companion.getRational
import zechs.zplex.ui.activity.player.Utils.Companion.hideSystemUI
import zechs.zplex.ui.activity.player.Utils.Companion.isPiPSupported
import zechs.zplex.ui.activity.player.Utils.Companion.isPortrait
import zechs.zplex.ui.activity.player.Utils.Companion.setButtonEnabled
import zechs.zplex.ui.activity.player.Utils.Companion.setOrientation
import zechs.zplex.ui.activity.player.Utils.Companion.setViewMargins
import zechs.zplex.ui.activity.player.Utils.Companion.setViewParams
import zechs.zplex.ui.activity.player.Utils.Companion.showSystemUI
import zechs.zplex.ui.activity.player.Utils.Companion.showText
import zechs.zplex.ui.activity.player.dtpv.DoubleTapPlayerView
import zechs.zplex.ui.activity.player.dtpv.youtube.YouTubeOverlay
import zechs.zplex.ui.activity.player.dtpv.youtube.YouTubeOverlay.PerformListener
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class PlayerActivity : AppCompatActivity() {

    private val rationalLimitWide = Rational(239, 100)
    private val rationalLimitTall = Rational(100, 239)
    var frameRendered = false
    lateinit var chapterStarts: LongArray
    var apiAccess = false
    var playbackFinished = false
    var displayManager: DisplayManager? = null
    var displayListener: DisplayListener? = null
    private var playerListener: PlayerListener? = null
    private var mReceiver: BroadcastReceiver? = null
    private var mAudioManager: AudioManager? = null
    private var mPictureInPictureParamsBuilder: Any? = null
    private var videoLoading = false
    private var errorToShow: ExoPlaybackException? = null
    private var scaleFactor = 1.0f

    private var loadingProgressBar: ProgressBar? = null
    private var restorePlayState = false
    private var play = false
    private var isScrubbing = false
    private var scrubbingNoticeable = false
    private var scrubbingStart: Long = 0
    private var lastScrubbingPosition: Long = 0
    private var orientation = Utils.Orientation.VIDEO

    private val driveStreamCookie = "DRIVE_STREAM="
    private var _isInPip = false
    private var tmdbId = 0
    private var name: String? = null
    private var mediaType: String? = null
    private var posterPath: String? = null
    private var seasonNumber = 0
    private var episodeNumber = 0

    private var buttonPiP: ImageButton? = null

    private lateinit var playerView: CustomStyledPlayerView
    private lateinit var timeBar: CustomDefaultTimeBar
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var titleView: TextView
    private lateinit var buttonAspectRatio: ImageButton
    private lateinit var exoSettings: ImageButton
    private lateinit var exoPlayPause: ImageButton
    private lateinit var youTubeOverlay: YouTubeOverlay

    //private PlayerViewModel playerViewModel;

    @SuppressLint("ClickableViewAccessibility", "PrivateResource", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        setOrientation(this, orientation)

//        WatchedRepository watchedRepository = new WatchedRepository(
//                WatchlistDatabase.Companion.invoke(this)
//        );
//
//        playerViewModel = new ViewModelProvider(
//                this,
//                new PlayerViewModelProviderFactory(watchedRepository)
//        ).get(PlayerViewModel.class);

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT == 28 && Build.MANUFACTURER.equals(
                "xiaomi",
                ignoreCase = true
            ) && Build.DEVICE.equals("oneday", ignoreCase = true)
        ) {
            setContentView(R.layout.activity_player_textureview)
        } else {
            setContentView(R.layout.activity_player)
        }

        focusPlay = true

        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        playerView = findViewById(R.id.video_view)
        exoPlayPause = findViewById(R.id.exo_play_pause)
        loadingProgressBar = findViewById(R.id.loading)

        playerView.apply {
            setShowNextButton(false)
            setShowPreviousButton(false)
            setShowFastForwardButton(false)
            setShowRewindButton(false)
            controllerHideOnTouch = false
            controllerAutoShow = true
            (this as DoubleTapPlayerView?)!!.isDoubleTapEnabled = false
        }

        timeBar = playerView.findViewById(R.id.exo_progress)
        timeBar.addListener(object : OnScrubListener {
            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                if (player == null) return
                restorePlayState = player!!.isPlaying
                if (restorePlayState) {
                    player!!.pause()
                }
                lastScrubbingPosition = position
                scrubbingNoticeable = false
                isScrubbing = true
                frameRendered = true
                playerView.controllerShowTimeoutMs = -1
                scrubbingStart = player!!.currentPosition
                player!!.setSeekParameters(SeekParameters.CLOSEST_SYNC)
                reportScrubbing(position)
            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                reportScrubbing(position)
                for (start in chapterStarts) {
                    if (start in (lastScrubbingPosition + 1)..position
                        || start in position until lastScrubbingPosition
                    ) {
                        playerView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }
                }
                lastScrubbingPosition = position
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                playerView.setCustomErrorMessage(null)
                isScrubbing = false
                if (restorePlayState) {
                    restorePlayState = false
                    playerView.controllerShowTimeoutMs = CONTROLLER_TIMEOUT
                    player!!.playWhenReady = true
                }
            }
        })

        if (isPiPSupported(this)) {
            // TODO: Android 12 improvements:
            // https://developer.android.com/about/versions/12/features/pip-improvements
            mPictureInPictureParamsBuilder = PictureInPictureParams.Builder()
            val success = updatePictureInPictureActions(
                R.drawable.ic_play_arrow_24dp,
                R.string.exo_controls_play_description,
                CONTROL_TYPE_PLAY,
                REQUEST_PLAY
            )

            if (success) {
                buttonPiP = ImageButton(this, null, 0, R.style.ExoStyledControls_Button_Bottom)
                buttonPiP!!.apply {
                    contentDescription = getString(R.string.button_pip)
                    setImageResource(R.drawable.ic_picture_in_picture_alt_24dp)
                    setOnClickListener { enterPiP() }
                }
            }
        }

        buttonAspectRatio = ImageButton(this, null, 0, R.style.ExoStyledControls_Button_Bottom)
        buttonAspectRatio.apply {
            contentDescription = getString(R.string.button_crop)
            setImageResource(R.drawable.ic_aspect_ratio_24)
            setOnClickListener {
                playerView.setScale(1f)
                if (playerView.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                    playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    showText(playerView, getString(R.string.video_resize_crop))
                } else {
                    // Default mode
                    playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    showText(playerView, getString(R.string.video_resize_fit))
                }
                resetHideCallbacks()
            }
        }

        val buttonRotation = ImageButton(this, null, 0, R.style.ExoStyledControls_Button_Bottom)
        buttonRotation.apply {
            contentDescription = getString(R.string.button_rotate)
            setImageResource(R.drawable.ic_auto_rotate_24dp)
            setOnClickListener {
                orientation = getNextOrientation(orientation)
                setOrientation(this@PlayerActivity, orientation)
                showText(playerView, getString(orientation.description), 2500)
                resetHideCallbacks()
            }
        }

        val titleViewPadding = resources.getDimensionPixelOffset(
            R.dimen.exo_styled_bottom_bar_time_padding
        )
        val typeface = ResourcesCompat.getFont(applicationContext, R.font.open_sans_semibold)
        val centerView = playerView.findViewById<FrameLayout>(R.id.exo_controls_background)

        titleView = TextView(this)
        titleView.apply {
            setTextColor(Color.WHITE)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(
                titleViewPadding,
                titleViewPadding,
                titleViewPadding,
                titleViewPadding
            )
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            visibility = View.GONE
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            textDirection = View.TEXT_DIRECTION_LOCALE
            setTypeface(typeface)
        }

        centerView.addView(titleView)

        val controlView = playerView.findViewById<StyledPlayerControlView>(R.id.exo_controller)
        controlView.setOnApplyWindowInsetsListener { view: View, windowInsets: WindowInsets? ->
            if (windowInsets != null) {
                view.setPadding(
                    0, windowInsets.systemWindowInsetTop,
                    0, windowInsets.systemWindowInsetBottom
                )
                val insetLeft = windowInsets.systemWindowInsetLeft
                val insetRight = windowInsets.systemWindowInsetRight
                var paddingLeft = 0
                var marginLeft = insetLeft
                var paddingRight = 0
                var marginRight = insetRight
                if (Build.VERSION.SDK_INT >= 28 && windowInsets.displayCutout != null) {
                    if (windowInsets.displayCutout!!.safeInsetLeft == insetLeft) {
                        paddingLeft = insetLeft
                        marginLeft = 0
                    }
                    if (windowInsets.displayCutout!!.safeInsetRight == insetRight) {
                        paddingRight = insetRight
                        marginRight = 0
                    }
                }
                setViewParams(
                    titleView,
                    paddingLeft + titleViewPadding,
                    titleViewPadding,
                    paddingRight + titleViewPadding,
                    titleViewPadding,
                    marginLeft,
                    windowInsets.systemWindowInsetTop,
                    marginRight,
                    0
                )
                setViewParams(
                    findViewById(R.id.exo_bottom_bar), paddingLeft, 0, paddingRight, 0,
                    marginLeft, 0, marginRight, 0
                )
                findViewById<View>(R.id.exo_progress).setPadding(
                    windowInsets.systemWindowInsetLeft, 0,
                    windowInsets.systemWindowInsetRight, 0
                )
                setViewMargins(
                    findViewById(R.id.exo_error_message),
                    0,
                    windowInsets.systemWindowInsetTop / 2,
                    0,
                    resources.getDimensionPixelSize(R.dimen.exo_error_message_margin_bottom) + windowInsets.systemWindowInsetBottom / 2
                )
                windowInsets.consumeSystemWindowInsets()
            }
            windowInsets
        }

        try {
            val customDefaultTrackNameProvider = CustomDefaultTrackNameProvider(resources)
            val field = StyledPlayerControlView::class.java.getDeclaredField("trackNameProvider")
            field.isAccessible = true
            field[controlView] = customDefaultTrackNameProvider
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        exoPlayPause.setOnClickListener { dispatchPlayPause() }

        // Prevent double tap actions in controller
        findViewById<View>(R.id.exo_bottom_bar).setOnTouchListener { _: View?, _: MotionEvent? -> true }
        //titleView.setOnTouchListener((v, event) -> true);

        playerListener = PlayerListener()

        val exoBasicControls = playerView.findViewById<LinearLayout>(R.id.exo_basic_controls)
        val exoSubtitle = exoBasicControls.findViewById<ImageButton>(R.id.exo_subtitle)
        exoBasicControls.removeView(exoSubtitle)

        exoSettings = exoBasicControls.findViewById(R.id.exo_settings)
        exoBasicControls.removeView(exoSettings)
        updateButtons(false)
        val horizontalScrollView =
            layoutInflater.inflate(R.layout.controls, null) as HorizontalScrollView
        val controls = horizontalScrollView.findViewById<LinearLayout>(R.id.controls)
        controls.addView(buttonRotation)
        if (isPiPSupported(this) && buttonPiP != null) {
            controls.addView(buttonPiP)
        }
        controls.addView(buttonAspectRatio)
        controls.addView(exoSubtitle)
        controls.addView(exoSettings)
        exoBasicControls.addView(horizontalScrollView)

        horizontalScrollView.setOnScrollChangeListener { _: View?, _: Int, _: Int, _: Int, _: Int ->
            resetHideCallbacks()

        }
        playerView.setControllerVisibilityListener { visibility: Int ->
            controllerVisible = visibility == View.VISIBLE
            controllerVisibleFully = playerView.isControllerFullyVisible
            if (restoreControllerTimeout) {
                restoreControllerTimeout = false
                if (player == null || !player!!.isPlaying) {
                    playerView.controllerShowTimeoutMs = -1
                } else {
                    playerView.setControllerShowTimeoutMs(CONTROLLER_TIMEOUT)
                }
            }
            if (visibility == View.VISIBLE) {
                showSystemUI(window, playerView)
                findViewById<View>(R.id.exo_play_pause).requestFocus()
            } else {
                hideSystemUI(window, playerView)
            }
            if (controllerVisible && playerView.isControllerFullyVisible) {
                if (errorToShow != null) {
                    showError(errorToShow!!)
                    errorToShow = null
                }
            }
        }
        youTubeOverlay = findViewById(R.id.youtube_overlay)
        youTubeOverlay.performListener(object : PerformListener {
            override fun onAnimationStart() {
                youTubeOverlay.alpha = 1.0f
                youTubeOverlay.visibility = View.VISIBLE
            }

            override fun onAnimationEnd() {
                youTubeOverlay.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            youTubeOverlay.visibility = View.GONE
                            youTubeOverlay.alpha = 1.0f
                        }
                    })
            }
        })
        initializePlayer()
    }

    override fun onStop() {
        saveProgress()
        super.onStop()
    }

    override fun onDestroy() {
        saveProgress()
        releasePlayer()
        super.onDestroy()
    }

    private fun saveProgress() {
        val watchedDuration = player!!.currentPosition
        val totalDuration = player!!.duration
        val watchProgress = (watchedDuration.toDouble() / totalDuration.toDouble()).toFloat() * 100

//        if (watchProgress > 10) {
//
//            playerViewModel.upsertWatched(
//                    tmdbId,
//                    name,
//                    mediaType,
//                    posterPath,
//                    seasonNumber,
//                    episodeNumber,
//                    watchedDuration,
//                    totalDuration);
//        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        initializePlayer()
    }

    private fun getStreamUrl(fileId: String): Uri {
        return Uri.parse("https://www.googleapis.com/drive/v3/files/$fileId?supportsAllDrives=True&alt=media")
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (0 != event.source and InputDevice.SOURCE_CLASS_POINTER) {
            if (event.action == MotionEvent.ACTION_SCROLL) {
                val value = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                adjustVolume(mAudioManager!!, playerView, value > 0.0f)
                return true
            }
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            // On Android TV it is required to hide controller in this PIP change callback
            playerView.hideController()
            playerView.setScale(1f)
            mReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (ACTION_MEDIA_CONTROL != intent.action || player == null) {
                        return
                    }
                    when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                        CONTROL_TYPE_PLAY -> player!!.play()
                        CONTROL_TYPE_PAUSE -> player!!.pause()
                    }
                }
            }
            registerReceiver(mReceiver, IntentFilter(ACTION_MEDIA_CONTROL))
        } else {
            if (mReceiver != null) {
                unregisterReceiver(mReceiver)
                mReceiver = null
            }
            playerView.controllerAutoShow = true
            if (player != null) {
                if (player!!.isPlaying) hideSystemUI(
                    window, playerView
                ) else playerView.showController()
            }
        }
    }

    private fun initializePlayer() {

        val fileId = intent.getStringExtra("fileId")
        val title = intent.getStringExtra("title")
        val accessToken = intent.getStringExtra("accessToken")

        tmdbId = intent.getIntExtra("tmdbId", 0)
        name = intent.getStringExtra("name")
        mediaType = intent.getStringExtra("mediaType")
        posterPath = intent.getStringExtra("posterPath")
        seasonNumber = intent.getIntExtra("seasonNumber", 0)
        episodeNumber = intent.getIntExtra("episodeNumber", 0)

        val mediaUri = getStreamUrl(fileId!!)

        titleView.text = title
        focusPlay = true
        haveMedia = true

        player?.apply {
            removeListener(playerListener!!)
            clearMediaItems()
            release()
            player = null
        }

        val trackSelector = DefaultTrackSelector(this)
        trackSelector.setParameters(
            trackSelector.buildUponParameters()
                .setPreferredAudioLanguages(*deviceLanguages)
        )

        val extractorsFactory = DefaultExtractorsFactory()
            .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS)
            .setTsExtractorTimestampSearchBytes(1500 * TsExtractor.TS_PACKET_SIZE)

        val renderersFactory: RenderersFactory = DefaultRenderersFactory(this)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

        val playerBuilder = ExoPlayer.Builder(this, renderersFactory)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this, extractorsFactory))

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        httpDataSourceFactory.setAllowCrossProtocolRedirects(true)

        val dataSourceFactory = DataSource.Factory {
            val dataSource = httpDataSourceFactory.createDataSource()
            if (driveStreamCookie != "DRIVE_STREAM=") {
                Log.d("PlayerActivity", driveStreamCookie)
                dataSource.setRequestProperty("Cookie", driveStreamCookie)
            } else {
                dataSource.setRequestProperty(
                    "Authorization",
                    "Bearer $accessToken"
                )
            }
            dataSource
        }

        playerBuilder.setMediaSourceFactory(
            DefaultMediaSourceFactory(dataSourceFactory, extractorsFactory)
        )

        player = playerBuilder.build()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build()

        player!!.setAudioAttributes(audioAttributes, true)
        val subtitleView = playerView.subtitleView
        if (subtitleView != null) {
            subtitleView.setApplyEmbeddedFontSizes(false)
            subtitleView.setApplyEmbeddedStyles(false)
        }
        youTubeOverlay.player(player)
        playerView.player = player
        playerView.controllerShowTimeoutMs = -1
        locked = false
        chapterStarts = LongArray(0)
        if (haveMedia) {
            timeBar.setBufferedColor(DefaultTimeBar.DEFAULT_BUFFERED_COLOR)
            val RESIZE_MODE = AspectRatioFrameLayout.RESIZE_MODE_FIT
            playerView.resizeMode = RESIZE_MODE
            playerView.setScale(1f)
            val mediaItemBuilder = MediaItem.Builder().setUri(mediaUri)
            Log.d("mediaUri", mediaUri.toString())
            player!!.setMediaItem(mediaItemBuilder.build())
            player!!.play()
            notifyAudioSessionUpdate(true)
            videoLoading = true
            updateLoading(true)
            if (apiAccess) {
                play = true
            }
            titleView.text = title
            titleView.visibility = View.VISIBLE
            updateButtons(true)
            (playerView as DoubleTapPlayerView?)!!.isDoubleTapEnabled = true

            // Utils.Companion.markChapters(this, mPrefs.mediaUri, controlView);
            player!!.setHandleAudioBecomingNoisy(true)
        } else {
            playerView.showController()
        }
        player!!.addListener(
            playerListener!!
        )
        player!!.prepare()
        if (restorePlayState) {
            restorePlayState = false
            playerView.showController()
            player!!.play()
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            notifyAudioSessionUpdate(false)
            if (player!!.isPlaying) {
                restorePlayState = true
            }
            player!!.removeListener(
                playerListener!!
            )
            player!!.clearMediaItems()
            player!!.release()
            player = null
        }
        titleView.visibility = View.GONE
        updateButtons(false)
    }

    fun updatePictureInPictureActions(
        iconId: Int,
        resTitle: Int,
        controlType: Int,
        requestCode: Int
    ): Boolean {
        try {
            val actions = ArrayList<RemoteAction>()
            val intent = PendingIntent.getBroadcast(
                this@PlayerActivity,
                requestCode,
                Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType),
                PendingIntent.FLAG_IMMUTABLE
            )
            val icon = Icon.createWithResource(this@PlayerActivity, iconId)
            val title = getString(resTitle)
            actions.add(RemoteAction(icon, title, title, intent))
            (mPictureInPictureParamsBuilder as PictureInPictureParams.Builder?)!!.setActions(actions)
            setPictureInPictureParams((mPictureInPictureParamsBuilder as PictureInPictureParams.Builder?)!!.build())
            return true
        } catch (e: IllegalStateException) {
            // On Samsung devices with Talkback active:
            // Caused by: java.lang.IllegalStateException: setPictureInPictureParams: Device doesn't support picture-in-picture mode.
            e.printStackTrace()
        }
        return false
    }

    fun showError(error: ExoPlaybackException) {
        val errorGeneral = error.localizedMessage
        val errorDetailed = when (error.type) {
            ExoPlaybackException.TYPE_SOURCE -> error.sourceException.localizedMessage
            ExoPlaybackException.TYPE_RENDERER -> error.rendererException.localizedMessage
            ExoPlaybackException.TYPE_UNEXPECTED -> error.unexpectedException.localizedMessage
            ExoPlaybackException.TYPE_REMOTE -> errorGeneral
            else -> errorGeneral
        }
        showSnack(errorGeneral, errorDetailed)
    }

    private fun showSnack(textPrimary: String?, textSecondary: String?) {
        snackbar = Snackbar.make(
            coordinatorLayout, textPrimary!!, Snackbar.LENGTH_LONG
        )
        if (textSecondary != null) {
            snackbar!!.setAction(R.string.error_details) { v: View? ->
                val builder = AlertDialog.Builder(this@PlayerActivity)
                builder.setMessage(textSecondary)
                builder.setPositiveButton(android.R.string.ok) { d: DialogInterface, _: Int ->
                    d.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }
        }
        snackbar!!.setAnchorView(R.id.exo_bottom_bar)
        snackbar!!.show()
    }

    fun reportScrubbing(position: Long) {
        val diff = position - scrubbingStart
        if (abs(diff) > 1000) {
            scrubbingNoticeable = true
        }
        if (scrubbingNoticeable) {
            playerView.clearIcon()
            playerView.setCustomErrorMessage(formatMillisSign(diff))
        }
        if (frameRendered) {
            frameRendered = false
            player!!.seekTo(position)
        }
    }

    private fun resetHideCallbacks() {
        if (haveMedia && player != null && player!!.isPlaying) {
            // Keep controller UI visible - alternative to resetHideCallbacks()
            playerView.controllerShowTimeoutMs =
                CONTROLLER_TIMEOUT
        }
    }

    private fun updateLoading(enableLoading: Boolean) {
        if (enableLoading) {
            exoPlayPause.visibility = View.GONE
            loadingProgressBar!!.visibility = View.VISIBLE
        } else {
            loadingProgressBar!!.visibility = View.GONE
            exoPlayPause.visibility = View.VISIBLE
            if (focusPlay) {
                focusPlay = false
                exoPlayPause.requestFocus()
            }
        }
    }

    private fun enterPiP() {
        _isInPip = true
        val appOpsManager = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        if (AppOpsManager.MODE_ALLOWED != appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                Process.myUid(),
                packageName
            )
        ) {
            val intent = Intent(
                "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                Uri.fromParts("package", packageName, null)
            )
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
            return
        }
        if (player == null) {
            return
        }
        playerView.controllerAutoShow = false
        playerView.hideController()
        val format = player!!.videoFormat
        if (format != null) {
            // https://github.com/google/ExoPlayer/issues/8611
            // TODO: Test/disable on Android 11+
            val videoSurfaceView = playerView.videoSurfaceView
            if (videoSurfaceView is SurfaceView) {
                videoSurfaceView.holder.setFixedSize(format.width, format.height)
            }
            var rational = getRational(format)
            if (rational.toFloat() > rationalLimitWide.toFloat()) rational =
                rationalLimitWide else if (rational.toFloat() < rationalLimitTall.toFloat()) rational =
                rationalLimitTall
            (mPictureInPictureParamsBuilder as PictureInPictureParams.Builder?)!!.setAspectRatio(
                rational
            )
        }
        enterPictureInPictureMode((mPictureInPictureParamsBuilder as PictureInPictureParams.Builder).build())
    }

    private fun dispatchPlayPause() {
        if (player == null) return
        val state = player!!.playbackState
        if (state == Player.STATE_IDLE || state == Player.STATE_ENDED || !player!!.playWhenReady) {
            player!!.play()
            shortControllerTimeout = true
        } else player!!.pause()
    }

    private fun notifyAudioSessionUpdate(active: Boolean) {
        val intent =
            Intent(if (active) AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION else AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player!!.audioSessionId)
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        if (active) {
            intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MOVIE)
        }
        sendBroadcast(intent)
    }

    private fun updateButtons(enable: Boolean) {
        if (buttonPiP != null) {
            setButtonEnabled(buttonPiP!!, enable)
        }
        setButtonEnabled(buttonAspectRatio, enable)
        setButtonEnabled(exoSettings, enable)
    }


    override fun onPause() {
        saveProgress()
        super.onPause()
        if (!_isInPip && player != null) player!!.pause()
    }

    private inner class PlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            playerView.keepScreenOn = isPlaying
            if (isPiPSupported(this@PlayerActivity)) {
                if (isPlaying) {
                    updatePictureInPictureActions(
                        R.drawable.ic_pause_24dp,
                        R.string.exo_controls_pause_description,
                        CONTROL_TYPE_PAUSE,
                        REQUEST_PAUSE
                    )
                } else {
                    updatePictureInPictureActions(
                        R.drawable.ic_play_arrow_24dp,
                        R.string.exo_controls_play_description,
                        CONTROL_TYPE_PLAY,
                        REQUEST_PLAY
                    )
                }
            }
            if (!isScrubbing) {
                if (isPlaying) {
                    if (shortControllerTimeout) {
                        playerView.controllerShowTimeoutMs =
                            CONTROLLER_TIMEOUT / 3
                        shortControllerTimeout = false
                        restoreControllerTimeout = true
                    } else {
                        playerView.controllerShowTimeoutMs =
                            CONTROLLER_TIMEOUT
                    }
                } else {
                    playerView.controllerShowTimeoutMs = -1
                }
            }
            if (!isPlaying) {
                locked = false
            }
        }

        @SuppressLint("SourceLockedOrientationActivity")
        override fun onPlaybackStateChanged(state: Int) {
            val duration = player!!.duration
            if (state == Player.STATE_READY) {
                frameRendered = true
                if (videoLoading) {
                    videoLoading = false
                    val format = player!!.videoFormat
                    if (format != null) {
                        if (orientation === Utils.Orientation.VIDEO) {
                            if (isPortrait(format)) {
                                this@PlayerActivity.requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                            } else {
                                this@PlayerActivity.requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }
                        }
                    }
                    if (duration != C.TIME_UNSET && duration > TimeUnit.MINUTES.toMillis(20)) {
                        timeBar.setKeyTimeIncrement(TimeUnit.MINUTES.toMillis(1))
                    } else {
                        timeBar.setKeyCountIncrement(20)
                    }
                    if (displayManager != null) {
                        displayManager!!.unregisterDisplayListener(displayListener)
                    }
                    if (play) {
                        play = false
                        player!!.play()
                        playerView.hideController()
                    }
                    updateLoading(false)
                }
            } else if (state == Player.STATE_ENDED) {
                playbackFinished = true
                if (apiAccess) {
                    finish()
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            updateLoading(false)
            if (error is ExoPlaybackException) {
                if (controllerVisible && controllerVisibleFully) {
                    showError(error)
                } else {
                    errorToShow = error
                }
            }
        }
    }

    companion object {
        const val CONTROLLER_TIMEOUT = 3500
        private const val ACTION_MEDIA_CONTROL = "media_control"
        private const val EXTRA_CONTROL_TYPE = "control_type"
        private const val REQUEST_PLAY = 1
        private const val REQUEST_PAUSE = 2
        private const val CONTROL_TYPE_PLAY = 1
        private const val CONTROL_TYPE_PAUSE = 2
        var player: ExoPlayer? = null
        var haveMedia = false
        var controllerVisible = false
        var controllerVisibleFully = false
        var snackbar: Snackbar? = null
        var focusPlay = false
        var locked = false
        var restoreControllerTimeout = false
        var shortControllerTimeout = false
        var boostLevel = 0
    }
}