package zechs.zplex.ui.fragment.browse

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.adapter.SearchAdapter
import zechs.zplex.databinding.FragmentBrowseBinding
import zechs.zplex.models.dataclass.FilterArgs
import zechs.zplex.models.dataclass.MediaArgs
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.viewmodels.FiltersViewModel
import zechs.zplex.utils.Resource

class BrowseFragment : Fragment(R.layout.fragment_browse) {

    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!

    private val filterModel by activityViewModels<FiltersViewModel>()

    // private val showsViewModel by activityViewModels<ShowViewModel>()
    private lateinit var browseViewModel: BrowseViewModel

    private val browseAdapter by lazy { SearchAdapter() }

    private var isLoading = true
    private var isLastPage = true
    private val thisTAG = "BrowseFragment"
    private var _filters: FilterArgs? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBrowseBinding.bind(view)

        browseViewModel = (activity as ZPlexActivity).browseViewModel
        setupRecyclerView()

        filterModel.filterArgs.observe(viewLifecycleOwner, { filter ->
            _filters = filter
            browseViewModel.getBrowse(filter)
            browseAdapter.setOnItemClickListener {
                val action = BrowseFragmentDirections.actionBrowseFragmentToFragmentMedia(
                    MediaArgs(it.id, it.media_type ?: filter.mediaType, it)
                )
                findNavController().navigate(action)
            }
        })

        browseViewModel.browse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        pbBrowse.isVisible = false
                        rvBrowse.isVisible = true
                    }
                    response.data?.let { showsResponse ->
                        browseAdapter.differ.submitList(showsResponse.results.toList())
                        isLastPage = showsResponse.page == showsResponse.total_pages
                    }
                    isLoading = false
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        val errorMsg = if (message.isEmpty()) {
                            resources.getString(R.string.something_went_wrong)
                        } else message
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
                        pbBrowse.isVisible = true
//                        rvBrowse.isVisible = false
                    }
                    isLoading = true
                }
            }
        })

    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0) {
                val layoutManager = binding.rvBrowse.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.findLastCompletelyVisibleItemPosition() + 1
                val itemCount = layoutManager.itemCount

                Log.d(
                    "onScrolled",
                    "visibleItemCount=$visibleItemCount, itemCount=$itemCount," +
                            " isLoading=$isLoading, isLastPage=$isLastPage," +
                            " is_filters=${_filters == null}"
                )

                if (visibleItemCount == itemCount && !isLoading && !isLastPage) {
                    _filters?.let { browseViewModel.getBrowse(it) }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvBrowse.apply {
            adapter = browseAdapter
            layoutManager = GridLayoutManager(activity, 3)
            itemAnimator = null
            addOnScrollListener(this@BrowseFragment.scrollListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            rvBrowse.adapter = null
        }
        _binding = null
    }

}