package zechs.zplex.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import zechs.zplex.R
import zechs.zplex.databinding.ActivityPlayerBinding
import zechs.zplex.utils.SessionManager


class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var fullscreenContent: StyledPlayerView
    private val hideHandler = Handler(Looper.getMainLooper())
    private lateinit var exoPlayer: ExoPlayer

    private val hidePart2Runnable = Runnable {
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.hide(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            )
        } else {
            fullscreenContent.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

    }
    private val showPart2Runnable = Runnable { supportActionBar?.show() }
    private var isFullscreen: Boolean = false
    private val hideRunnable = Runnable { hide() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        fullscreenContent = binding.playerView
        fullscreenContent.setOnClickListener { toggle() }

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
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);

        exoPlayer = ExoPlayer.Builder(this, rendererFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
        playMedia()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delayedHide()
    }

    private fun toggle() {
        if (isFullscreen) hide() else show()
    }

    private fun hide() {
        supportActionBar?.hide()
        isFullscreen = false

        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, 300L)
    }

    private fun show() {
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.show(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            )
        } else {
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }

        isFullscreen = true

        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, 300L)
    }

    private fun delayedHide() {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, 100L)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        playMedia()
    }

    private fun playMedia() {
        val fileId = intent.getStringExtra("fileId")
        val title = intent.getStringExtra("title")

        val mediaItem = MediaItem.Builder()
            .setUri(getStreamUrl(fileId))
            .build()

        exoPlayer.apply {
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
                .build();
        }

        binding.playerView.apply {
            player = exoPlayer
            setShowNextButton(false)
            setShowPreviousButton(false)
            setShowFastForwardButton(true)
            setShowRewindButton(true)
            controllerHideOnTouch = false
            controllerAutoShow = true
            findViewById<TextView>(R.id.exo_video_title).text = title
            findViewById<ImageButton>(R.id.exo_aspect_button).setOnClickListener {
                if (binding.playerView.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                    binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                } else {
                    binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            }
        }
    }

    private fun getStreamUrl(fileId: String?): Uri {
        return Uri.parse(
            "https://www.googleapis.com/drive/v3/files/${fileId}?supportsAllDrives=True&alt=media"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

}