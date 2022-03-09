package zechs.zplex.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R

class LookupMovieDialog(
    context: Context,
    private val accentColor: Int?,
    private var requestMovie: (() -> Unit)? = null
) : Dialog(context) {

    private val thisTAG = "LookupMovieDialog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_custom)

        val btnYes = findViewById<MaterialButton>(R.id.btn_yes)
        val btnOk = findViewById<MaterialButton>(R.id.btn_ok)

        accentColor?.let { c ->
            val color = if (isDark(c)) lightUpColor(c) else c
            val tintColor = ColorStateList.valueOf(color)
            findViewById<ProgressBar>(R.id.progressBar2).indeterminateTintList = tintColor
            btnYes.backgroundTintList = tintColor
            btnYes.setTextColor(getContrastColor(color))
            btnOk.backgroundTintList = tintColor
            btnOk.setTextColor(getContrastColor(color))
        }

        btnYes.setOnClickListener {
            changeLayouts(loading = true, request = false, message = false)
            requestMovie?.invoke()
        }

        btnOk.setOnClickListener { dismiss() }
        findViewById<MaterialButton>(R.id.btn_no)?.setOnClickListener { dismiss() }
    }

    fun changeLayouts(loading: Boolean, request: Boolean, message: Boolean) {
        val rootView = findViewById<MaterialCardView>(R.id.dialog_root)
        val loadingView = findViewById<ConstraintLayout>(R.id.view_loading)
        val requestView = findViewById<ConstraintLayout>(R.id.view_request_movie)
        val messageView = findViewById<ConstraintLayout>(R.id.view_message)

        val transition = TransitionSet().apply {
            addTransition(
                MaterialSharedAxis(
                    MaterialSharedAxis.Y, true
                ).apply {
                    interpolator = LinearInterpolator()
                    duration = 300
                })

            addTransition(Fade().apply {
                interpolator = LinearInterpolator()
            })
        }

        TransitionManager.beginDelayedTransition(rootView, transition)
        loadingView.isVisible = loading
        requestView.isVisible = request
        messageView.isVisible = message
    }

    private fun getContrastColor(color: Int): Int {
        val y = (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.blue(color)) / 1000
        return if (y >= 128) Color.parseColor("#151515") else Color.parseColor("#DEFFFFFF")
    }

    private fun isDark(color: Int): Boolean {
        val luminance = ("%.5f".format(ColorUtils.calculateLuminance(color))).toFloat()
        val threshold = 0.09000
        val isDark = luminance < threshold
        Log.d(thisTAG, "luminance=$luminance, threshold=$threshold, isDark=$isDark")
        return isDark
    }

    private fun lightUpColor(color: Int): Int {
        return Color.HSVToColor(FloatArray(3).apply {
            Color.colorToHSV(color, this)
            this[2] *= 2.0f
        })
    }

}