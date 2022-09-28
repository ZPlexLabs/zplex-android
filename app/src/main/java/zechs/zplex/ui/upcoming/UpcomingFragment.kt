package zechs.zplex.ui.upcoming

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.FragmentUpcomingBinding
import zechs.zplex.ui.shared_adapters.detailed_media.DetailedMediaAdapter
import zechs.zplex.utils.ext.navigateSafe
import zechs.zplex.utils.state.Resource

class UpcomingFragment : Fragment() {

    companion object {
        const val TAG = "UpcomingFragment"
    }

    private var _binding: FragmentUpcomingBinding? = null
    private val binding get() = _binding!!

    private val upcomingViewModel by activityViewModels<UpcomingViewModel>()

    private val upcomingAdapter by lazy {
        DetailedMediaAdapter { navigateMedia(it) }
    }

    private var isLoading = true
    private var isLastPage = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpcomingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUpcomingBinding.bind(view)

        setupRecyclerView()

        upcomingViewModel.upcoming.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        pbBrowse.isVisible = false
                        rvBrowse.isVisible = true
                    }
                    response.data?.let { showsResponse ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            upcomingAdapter.submitList(showsResponse.results.toList())
                        }
                        isLastPage = showsResponse.page == showsResponse.total_pages
                    }
                    isLoading = false
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        val errorMsg = message.ifEmpty {
                            resources.getString(R.string.something_went_wrong)
                        }
                        Log.e(TAG, errorMsg)
                        binding.apply {
                            pbBrowse.isVisible = true
                            rvBrowse.isVisible = false
                            errorView.root.isVisible = true
                        }
                        binding.errorView.apply {
                            errorTxt.text = errorMsg
                        }
                        isLoading = false
                    }
                }
                is Resource.Loading -> {
                    binding.apply {
                        // pbBrowse.isVisible = true
                        // rvBrowse.isVisible = false
                    }
                    isLoading = true
                }
            }
        }

    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0) {
                val layoutManager = binding.rvBrowse.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.findLastCompletelyVisibleItemPosition() + 7
                val itemCount = layoutManager.itemCount

                Log.d(
                    "onScrolled",
                    "visibleItemCount=$visibleItemCount, itemCount=$itemCount," +
                            " isLoading=$isLoading, isLastPage=$isLastPage"
                )

                if (visibleItemCount >= itemCount && !isLoading && !isLastPage) {
                    upcomingViewModel.getUpcoming()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvBrowse.apply {
            adapter = upcomingAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
            addOnScrollListener(this@UpcomingFragment.scrollListener)
        }
    }


    private fun navigateMedia(media: Media) {
        val action = UpcomingFragmentDirections.actionUpcomingFragmentToFragmentMedia(
            media.copy(media_type = media.media_type ?: "movie")
        )
        findNavController().navigateSafe(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            rvBrowse.adapter = null
        }
        _binding = null
    }

}