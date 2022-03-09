package zechs.zplex.ui.activity.player

import android.app.Activity
import android.util.Log
import android.view.WindowManager

class BrightnessControl(
    private val activity: Activity
) {

    var currentBrightnessLevel = -1

    fun getScreenBrightness(): Int {
        return activity.window.attributes.screenBrightness.toInt()
    }

    private fun setScreenBrightness(brightness: Float) {
        val lp = activity.window.attributes
        lp.screenBrightness = brightness
        activity.window.attributes = lp
    }

    fun changeBrightness(
        playerView: CustomStyledPlayerView,
        increase: Boolean,
        canSetAuto: Boolean
    ) {
        val newBrightnessLevel = if (increase) {
            currentBrightnessLevel++
        } else currentBrightnessLevel--

        if (canSetAuto && newBrightnessLevel < 0) {
            currentBrightnessLevel = -1
        } else if (newBrightnessLevel in 0..30) {
            currentBrightnessLevel = newBrightnessLevel
        }

        if (currentBrightnessLevel == -1 && canSetAuto) {
            setScreenBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
        } else if (currentBrightnessLevel != -1) {
            setScreenBrightness(levelToBrightness(currentBrightnessLevel))
        }

        playerView.setHighlight(false)

        if (currentBrightnessLevel == -1 && canSetAuto) {
            playerView.setIconBrightnessAuto()
            playerView.setCustomErrorMessage("")
        } else {
            playerView.setIconBrightness()
            playerView.setCustomErrorMessage(" $currentBrightnessLevel")
        }
        Log.d(
            "changeBrightness",
            "newBrightnessLevel=$newBrightnessLevel, increase=$increase, canSetAuto=$canSetAuto"
        )
    }

    private fun levelToBrightness(level: Int): Float {
        val d = 0.064 + 0.936 / 30.toDouble() * level.toDouble()
        return (d * d).toFloat()
    }
}