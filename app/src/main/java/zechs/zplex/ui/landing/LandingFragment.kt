package zechs.zplex.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import zechs.zplex.R
import zechs.zplex.databinding.FragmentLandingBinding

class LandingFragment : Fragment() {

    companion object {
        const val TAG = "LandingFragment"
    }

    private var _binding: FragmentLandingBinding? = null
    private val binding get() = _binding!!

    private lateinit var childNavController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val childNavHost = childFragmentManager.findFragmentById(R.id.childNavHostFragment) as NavHostFragment
        childNavController = childNavHost.navController
        binding.bottomNavigationView.setupWithNavController(childNavController)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (!childNavController.popBackStack()) {
                isEnabled = false
                requireActivity().onNavigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}