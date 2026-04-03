package zechs.zplex.feature_home.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import zechs.zplex.feature_home.R
import zechs.zplex.feature_home.databinding.FragmentHomeBinding
import zechs.zplex.feature_home.ui.bottomsheet.HomeMenuBottomSheet

class HomeFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_user -> {
                    showUserMenu()
                    true
                }

                else -> false
            }
        }
    }


    private fun showUserMenu() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect { user ->

                    if (user == null) {
                        Log.d(TAG, "User is null, not opening sheet")
                        return@collect
                    }

                    HomeMenuBottomSheet(
                        user = user,
                        onProfileClick = { },
                        onLogoutClick = { viewModel.logout() }
                    ).show(childFragmentManager, "home_menu")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}