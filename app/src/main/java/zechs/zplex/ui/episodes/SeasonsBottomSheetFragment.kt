package zechs.zplex.ui.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import zechs.zplex.databinding.BottomSeasonsSheetFragmentBinding
import zechs.zplex.ui.shared_adapters.season.SeasonsAdapter
import zechs.zplex.utils.state.Resource

@AndroidEntryPoint
class SeasonsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSeasonsSheetFragmentBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel by activityViewModels<EpisodesSharedViewModel>()
    private val seasonsAdapter by lazy {
        SeasonsAdapter(
            showName = sharedViewModel.showName,
            seasonOnClick = {
                sharedViewModel.selectSeason(it.season_number)
                dismiss()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSeasonsSheetFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSeasonsObserver()
    }

    private fun setupSeasonsObserver() {
        sharedViewModel.seasons.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    TransitionManager.beginDelayedTransition(
                        binding.root,
                        MaterialFadeThrough()
                    )
                    seasonsAdapter.submitList(it.data)
                    isLoading(false)
                }

                is Resource.Error -> {
                    isLoading(true)
                }

                is Resource.Loading -> {
                    isLoading(true)
                }
            }
        }
    }

    private fun isLoading(hide: Boolean) {
        binding.apply {
            loading.isInvisible = !hide
            rvList.isInvisible = hide
        }
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = seasonsAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
