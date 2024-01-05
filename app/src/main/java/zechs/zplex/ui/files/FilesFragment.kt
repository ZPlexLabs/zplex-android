package zechs.zplex.ui.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.databinding.FragmentFilesBinding

class FilesFragment : Fragment() {

    companion object {
        const val TAG = "FilesFragment"
    }

    private var _binding: FragmentFilesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(
            /* axis */ MaterialSharedAxis.X,
            /* forward */ true
        ).apply {
            interpolator = LinearInterpolator()
            duration = 300
        }

        returnTransition = MaterialSharedAxis(
            /* axis */ MaterialSharedAxis.X,
            /* forward */ false
        ).apply {
            interpolator = LinearInterpolator()
            duration = 300
        }

        exitTransition = MaterialSharedAxis(
            /* axis */ MaterialSharedAxis.X,
            /* forward */ true
        ).apply {
            interpolator = LinearInterpolator()
            duration = 250
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilesBinding.inflate(
            inflater, container, /* attachToParent */false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFilesBinding.bind(view)

        // Workaround for transition animation
        // https://github.com/material-components/material-components-android/issues/1984
        val colorBackground = MaterialColors.getColor(view, android.R.attr.colorBackground)
        view.setBackgroundColor(colorBackground)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.rvList.adapter = null
        _binding = null
    }

}