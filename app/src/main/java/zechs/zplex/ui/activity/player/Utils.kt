package zechs.zplex.ui.activity.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.Format
import zechs.zplex.BuildConfig
import zechs.zplex.R
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal class Utils {

    companion object {

        fun dpToPx(dp: Int) = (dp * Resources.getSystem().displayMetrics.density).toInt()
        fun pxToDp(px: Float) = px / Resources.getSystem().displayMetrics.density

        fun hideSystemUI(window: Window, playerView: CustomStyledPlayerView) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(
                window, playerView
            ).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        fun showSystemUI(window: Window, playerView: CustomStyledPlayerView) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(
                window,
                playerView
            ).show(WindowInsetsCompat.Type.systemBars())
        }

        fun isVolumeMax(audioManager: AudioManager) = audioManager.getStreamVolume(
            AudioManager.STREAM_MUSIC
        ) == audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        fun adjustVolume(
            audioManager: AudioManager,
            playerView: CustomStyledPlayerView,
            raise: Boolean,
        ) {

            playerView.removeCallbacks(playerView.textClearRunnable)
            val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            var volumeActive = volume != 0

            // Handle volume changes outside the app (lose boost if volume is not maxed out)
            if (volume != volumeMax) {
                PlayerActivity.boostLevel = 0
            }

            if (volume != volumeMax || PlayerActivity.boostLevel == 0 && !raise) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    if (raise) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                )
                val volumeNew = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (raise && volume == volumeNew && !isVolumeMin(audioManager)) {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE or AudioManager.FLAG_SHOW_UI
                    )
                } else {
                    volumeActive = volumeNew != 0
                    playerView.setCustomErrorMessage(if (volumeActive) " $volumeNew" else "")
                }
            }
            playerView.apply {
                setIconVolume(volumeActive)
                setHighlight(PlayerActivity.boostLevel > 0)
                postDelayed(
                    this.textClearRunnable,
                    CustomStyledPlayerView.MESSAGE_TIMEOUT_KEY.toLong()
                )
            }
        }

        fun setButtonEnabled(button: ImageButton, enabled: Boolean) {
            button.isEnabled = enabled
            button.alpha = if (enabled) 100f / 100 else 33f / 100
        }

        @JvmOverloads
        fun showText(playerView: CustomStyledPlayerView, text: String?, timeout: Long = 1200) {
            playerView.apply {
                removeCallbacks(this.textClearRunnable)
                clearIcon()
                setCustomErrorMessage(text)
                postDelayed(this.textClearRunnable, timeout)
            }
        }

        @SuppressLint("SourceLockedOrientationActivity")
        fun setOrientation(activity: Activity, orientation: Orientation?) {
            when (orientation) {
                Orientation.VIDEO -> if (PlayerActivity.player != null) {
                    val format = PlayerActivity.player.videoFormat
                    if (format != null && isPortrait(format)) {
                        activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                    } else {
                        activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    }
                } else {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
                Orientation.SENSOR -> activity.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR
                else -> {}
            }
        }


        fun getNextOrientation(orientation: Orientation?): Orientation {
            return when (orientation) {
                Orientation.VIDEO -> Orientation.SENSOR
                Orientation.SENSOR -> Orientation.VIDEO
                else -> Orientation.VIDEO
            }
        }

        fun isPortrait(format: Format): Boolean {
            return if (isRotated(format)) {
                format.width > format.height
            } else {
                format.height > format.width
            }
        }

        fun getRational(format: Format): Rational {
            return if (isRotated(format)) {
                Rational(format.height, format.width)
            } else Rational(format.width, format.height)
        }

        fun formatMillis(time: Long): String {
            val totalSeconds = abs(time.toInt() / 1000)
            val seconds = totalSeconds % 60
            val minutes = totalSeconds % 3600 / 60
            val hours = totalSeconds / 3600

            return if (hours > 0) String.format(
                Locale.getDefault(),
                "%d:%02d:%02d",
                hours, minutes, seconds
            ) else String.format(
                Locale.getDefault(),
                "%02d:%02d", minutes, seconds
            )
        }


        fun formatMillisSign(time: Long): String {
            return if (time > -1000 && time < 1000) formatMillis(time) else (if (time < 0) "−" else "+") + formatMillis(
                time
            )
        }

        fun setViewMargins(
            view: View,
            marginLeft: Int,
            marginTop: Int,
            marginRight: Int,
            marginBottom: Int
        ) {
            val layoutParams = view.layoutParams as FrameLayout.LayoutParams
            layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom)
            view.layoutParams = layoutParams
        }

        fun setViewParams(
            view: View,
            paddingLeft: Int,
            paddingTop: Int,
            paddingRight: Int,
            paddingBottom: Int,
            marginLeft: Int,
            marginTop: Int,
            marginRight: Int,
            marginBottom: Int
        ) {
            view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
            setViewMargins(view, marginLeft, marginTop, marginRight, marginBottom)
        }


        fun isPiPSupported(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        }

        val deviceLanguages: Array<String>
            get() {
                val locales: MutableList<String> = ArrayList()
                val localeList = Resources.getSystem().configuration.locales
                for (i in 0 until localeList.size()) {
                    locales.add(localeList[i].isO3Language)
                }
                return locales.toTypedArray()
            }


        fun normalizeScaleFactor(scaleFactor: Float): Float {
            return max(0.25f, min(scaleFactor, 2.0f))
        }


        private fun isVolumeMin(audioManager: AudioManager): Boolean {
            val min = if (Build.VERSION.SDK_INT >= 28) {
                audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
            } else 0
            return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == min
        }

        private fun isRotated(format: Format) =
            format.rotationDegrees == 90 || format.rotationDegrees == 270

        fun log(text: String?) {
            if (BuildConfig.DEBUG) {
                Log.d(BuildConfig.APPLICATION_ID, text!!)
            }
        }

    }

    enum class Orientation(
        val value: Int, val description: Int
    ) {
        VIDEO(0, R.string.video_orientation_video), SENSOR(1, R.string.video_orientation_sensor);
    }
}