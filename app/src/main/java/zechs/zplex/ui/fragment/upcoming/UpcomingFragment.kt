package zechs.zplex.ui.fragment.upcoming

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.adapter.shared_adapters.detailed_media.DetailedMediaAdapter
import zechs.zplex.databinding.FragmentUpcomingBinding
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.ui.BaseFragment
import zechs.zplex.ui.activity.main.MainActivity
import zechs.zplex.utils.Resource
import zechs.zplex.utils.navigateSafe

class UpcomingFragment : BaseFragment() {

    private var _binding: FragmentUpcomingBinding? = null
    private val binding get() = _binding!!

    private lateinit var upcomingViewModel: UpcomingViewModel

    private val upcomingAdapter by lazy {
        DetailedMediaAdapter { navigateMedia(it) }
    }

    private var isLoading = true
    private var isLastPage = true
    private val thisTAG = "UpcomingFragment"

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

        upcomingViewModel = (activity as MainActivity).upcomingViewModel
        setupRecyclerView()

        upcomingViewModel.upcoming.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        pbBrowse.isVisible = false
                        rvBrowse.isVisible = true
                    }
                    response.data?.let { showsResponse ->
                        upcomingAdapter.submitList(showsResponse.results.toList())
                        isLastPage = showsResponse.page == showsResponse.total_pages
                    }
                    isLoading = false
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        val errorMsg = message.ifEmpty {
                            resources.getString(R.string.something_went_wrong)
                        }
                        Log.e(thisTAG, errorMsg)
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