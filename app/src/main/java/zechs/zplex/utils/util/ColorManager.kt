package zechs.zplex.utils.util

import android.graphics.Color
import android.util.Log
import androidx.core.graphics.ColorUtils

class ColorManager {

    companion object {

        private const val TAG = "ColorManager"

        fun getContrastColor(color: Int): Int {
            val red = 299 * Color.red(color)
            val green = 587 * Color.green(color)
            val blue = 114 * Color.blue(color)
            val y = (red + green + blue) / 1000
            return if (y >= 128) {
                Color.parseColor("#1C1B1F")
            } else Color.parseColor("#DEFFFFFF")
        }

        fun isDark(color: Int): Boolean {
            val luminance = ("%.5f".format(ColorUtils.calculateLuminance(color))).toFloat()
            val threshold = 0.09000
            val isDark = luminance < threshold
            Log.d(
                TAG, "color=$color, luminance=$luminance, " +
                        "threshold=$threshold, isDark=$isDark"
            )
            return isDark
        }

        fun lightUpColor(color: Int): Int {
            val litColor = Color.HSVToColor(FloatArray(3).apply {
                Color.colorToHSV(color, this)
                this[2] *= 4.0f
            })
            Log.d(TAG, "litColor=$litColor")
            return litColor
        }

    }

}