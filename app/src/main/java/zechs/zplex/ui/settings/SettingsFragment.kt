package zechs.zplex.ui.settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.databinding.FragmentSettingsBinding
import zechs.zplex.utils.ext.navigateSafe


class SettingsFragment : Fragment() {

    companion object {
        const val TAG = "SettingsFragment"
    }

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(
            /* axis */ MaterialSharedAxis.Y,
            /* forward */ true
        ).apply {
            interpolator = LinearInterpolator()
            duration = 300
        }

        returnTransition = MaterialSharedAxis(
            /* axis */ MaterialSharedAxis.Y,
            /* forward */ false
        ).apply {
            interpolator = LinearInterpolator()
            duration = 300
        }

        exitTransition = MaterialSharedAxis(
            /* axis */ MaterialSharedAxis.Y,
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
        _binding = FragmentSettingsBinding.inflate(
            inflater, container, /* attachToParent */false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.settingConfigureClient.setOnClickListener {
            findNavController().navigateSafe(R.id.action_settingsFragment_to_signInFragment)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}