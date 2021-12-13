package zechs.zplex.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import zechs.zplex.R
import zechs.zplex.databinding.ActivityPlayerBinding
import zechs.zplex.utils.SessionManager


class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

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
            setShowNextButton(false)
            setShowPreviousButton(false)
            setShowFastForwardButton(true)
            setShowRewindButton(true)
            controllerHideOnTouch = false
            controllerAutoShow = true
            val titleText = findViewById<TextView>(R.id.exo_video_title)
            titleText.text = title
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
//        val totalTime = exoPlayer.duration
//        val watchedTime = exoPlayer.currentPosition
//        val hasWatched = watchedTime > totalTime / 2
        exoPlayer.release()
        super.onDestroy()
    }

}