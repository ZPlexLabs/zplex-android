package zechs.zplex.utils

import android.view.animation.PathInterpolator

// https://m3.material.io/styles/motion/easing-and-duration/tokens-specs
class MaterialMotionInterpolator {
    companion object {
        fun getEmphasizedInterpolator() = PathInterpolator(0.16f, 0.4f, 0.20f, 0.82f)
        fun getEmphasizedDecelerateInterpolator() = PathInterpolator(0.05f, 0.7f, 0.1f, 1f)
        fun getEmphasizedAccelerateInterpolator() = PathInterpolator(0.3f, 0f, 0.8f, 0.15f)
    }
}