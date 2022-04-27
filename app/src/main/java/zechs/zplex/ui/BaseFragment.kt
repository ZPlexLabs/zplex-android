package zechs.zplex.ui

import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        enterTransition = TransitionSet().apply {
//            addTransition(
//                MaterialSharedAxis(
//                    MaterialSharedAxis.Y, true
//                ).apply {
//                    interpolator = LinearInterpolator()
//                    duration = 500
//                })
//
//            addTransition(Fade().apply {
//                interpolator = LinearInterpolator()
//            })
//            enterTransitionListener?.let { addListener(it) }
//        }
//
//        exitTransition = MaterialSharedAxis(
//            MaterialSharedAxis.Y, true
//        ).apply {
//            interpolator = LinearInterpolator()
//            duration = 500
//        }
//
//        returnTransition = MaterialSharedAxis(
//            MaterialSharedAxis.Y, false
//        ).apply {
//            interpolator = LinearInterpolator()
//            duration = 220
//        }
    }

}