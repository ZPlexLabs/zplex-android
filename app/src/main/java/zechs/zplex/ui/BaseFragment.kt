package zechs.zplex.ui

import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionSet
import com.google.android.material.transition.MaterialSharedAxis

abstract class BaseFragment : Fragment() {

    abstract val enterTransitionListener: Transition.TransitionListener?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = TransitionSet().apply {
            addTransition(
                MaterialSharedAxis(
                    MaterialSharedAxis.Y, true
                ).apply {
                    interpolator = LinearInterpolator()
                    duration = 500
                })

            addTransition(Fade().apply {
                interpolator = LinearInterpolator()
            })
            enterTransitionListener?.let { addListener(it) }
        }

        exitTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, true
        ).apply {
            interpolator = LinearInterpolator()
            duration = 500
        }

        returnTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, false
        ).apply {
            interpolator = LinearInterpolator()
            duration = 220
        }
    }

}