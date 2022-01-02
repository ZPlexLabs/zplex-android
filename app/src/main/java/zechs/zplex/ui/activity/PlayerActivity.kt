package zechs.zplex.ui.activity

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import zechs.zplex.R
import zechs.zplex.databinding.ActivityPlayerBinding
import zechs.zplex.utils.SessionManager


class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var exoPlayer: ExoPlayer
    private var onStopCalled = false

    override fun onCreate(savedInstanceState: Bundle?) {

        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        findViewById<View>(android.R.id.content).transitionName = "shared_exoplayer"
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 250L
        }

        super.onCreate(savedInstanceState)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        val httpDataSourceFactory = DefaultHttpDataSource
            .Factory()
            .setAllowCrossProtocolRedirects(true)

        val dataSourceFactory = DataSource.Factory {
            val dataSource = httpDataSourceFactory.createDataSource()
            dataSource.setRequestProperty(
                "Authorization",
                "Bearer ${SessionManager(this).fetchAuthToken()}"
            )
            dataSource
        }

        val rendererFactory = DefaultRenderersFactory(this)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

        exoPlayer = ExoPlayer.Builder(this, rendererFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()

        playMedia()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        playMedia()
    }

    private fun playMedia() {
        val fileId = intent.getStringExtra("fileId")
        val title = intent.getStringExtra("title")

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(getStreamUrl(fileId))
            .build()

        exoPlayer.apply {
            setAudioAttributes(audioAttributes, true)
            addMediaItem(mediaItem)
            prepare()
            play()
            addListener(
                object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        Toast.makeText(
                            applicationContext, error.message, Toast.LENGTH_LONG
                        ).show()
                    }
                })

            trackSelectionParameters = this.trackSelectionParameters
                .buildUpon()
                .setPreferredAudioLanguage("en")
                .build()
        }

        binding.playerView.apply {
            player = exoPlayer
            setShowFastForwardButton(true)
            setShowRewindButton(true)
            controllerShowTimeoutMs = 0

            setControllerVisibilityListener {
                if (it == 0) showSystemUI() else hideSystemUI()
            }

            val titleText = findViewById<TextView>(R.id.exo_video_title)
            titleText.text = title
            findViewById<ImageButton>(R.id.exo_aspect_button).setOnClickListener {
                if (binding.playerView.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                    binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                } else {
                    binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            }
            findViewById<ImageButton>(R.id.exo_pip_button).setOnClickListener {
                enterPIPMode()
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        binding.playerView.apply {
            controllerAutoShow = !isInPictureInPictureMode
            if (isInPictureInPictureMode) hideController() else showController()
        }
        if (onStopCalled) {
            finish()
        }
    }

    private fun getStreamUrl(fileId: String?): Uri {
        return Uri.parse(
            "https://www.googleapis.com/drive/v3/files/${
                fileId
            }?supportsAllDrives=True&alt=media"
        )
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(
            window, window.decorView
        ).show(WindowInsetsCompat.Type.systemBars())
    }

    private fun enterPIPMode() {
        this.enterPictureInPictureMode(
            PictureInPictureParams.Builder()
                .build()
        )
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPIPMode()
    }

    override fun onStop() {
        super.onStop()
        onStopCalled = true
    }

    override fun onResume() {
        super.onResume()
        onStopCalled = false
    }

    override fun onDestroy() {
//        val totalTime = exoPlayer.duration
//        val watchedTime = exoPlayer.currentPosition
//        val hasWatched = watchedTime > totalTime / 2
        binding.playerView.player = null
        exoPlayer.release()
        super.onDestroy()
    }
}