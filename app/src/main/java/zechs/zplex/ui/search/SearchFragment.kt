package zechs.zplex.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.FragmentSearchBinding
import zechs.zplex.ui.shared_adapters.media.MediaAdapter
import zechs.zplex.utils.Constants.SEARCH_DELAY_AMOUNT
import zechs.zplex.utils.ext.navigateSafe
import zechs.zplex.utils.ext.setupClearButtonWithAction
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.util.Keyboard


class SearchFragment : Fragment() {

    companion object {
        const val TAG = "SearchFragment"
    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel by activityViewModels<SearchViewModel>()

    private val searchAdapter by lazy {
        MediaAdapter(
            rating = true,
            mediaOnClick = { navigateToMedia(it) }
        )
    }
    private var queryText = ""
    private var isLoading = true
    var isLastPage = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        setupRecyclerView()

        binding.searchBar.setupClearButtonWithAction()
        var job: Job? = null

        binding.searchBar.apply {
            Keyboard.show(this)
            setText(queryText)
            addTextChangedListener { editable ->
                job?.cancel()
                job = MainScope().launch {
                    delay(SEARCH_DELAY_AMOUNT)
                    editable?.let {
                        val query = it.toString()
                        if (query.isNotEmpty() && query != queryText) {
                            searchViewModel.getSearch(query)
                        }
                        queryText = query
                    }
                }
            }
        }

        searchViewModel.searchResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        pbSearch.isInvisible = true
                        rvSearch.isInvisible = queryText.isEmpty()
                    }
                    response.data?.let { search ->
                        isLastPage = searchViewModel.page - 1 == search.total_pages

                        if (search.results.isEmpty()) {
                            Toast.makeText(
                                context, "Nothing found", Toast.LENGTH_SHORT
                            ).show()
                        }

                        val searchList = search.results.filter {
                            it.media_type != null && it.poster_path != null
                        }

                        viewLifecycleOwner.lifecycleScope.launch {
                            searchAdapter.submitList(searchList.toList())
                        }
                        isLoading = false
                    }
                }

                is Resource.Error -> {
                    isLoading = false
                    response.message?.let { message ->
                        Toast.makeText(
                            context, "An error occurred: $message", Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "An error occurred: $message")
                    }
                    binding.pbSearch.isInvisible = true
                }

                is Resource.Loading -> {
                    isLoading = true
                    binding.pbSearch.isVisible = true
                }
            }
        }

    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0) {
                val layoutManager = binding.rvSearch.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.findLastCompletelyVisibleItemPosition() + 1
                val itemCount = layoutManager.itemCount

                if (visibleItemCount == itemCount && !isLoading && !isLastPage) {
                    Log.d(
                        "onScrolled",
                        "visibleItemCount=$visibleItemCount, itemCount=$itemCount, " +
                                "isLoading=$isLoading isLastPage=$isLastPage"
                    )
                    searchViewModel.getSearch(queryText)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        val smallestWidthDp = resources.configuration.smallestScreenWidthDp

        Log.d("ScreenInfo", "Smallest screen width dp: $smallestWidthDp")

        val spanCount = resources.getInteger(R.integer.grid_span_count)
        val widthRatio = when (spanCount) {
            3 -> 0.30
            4 -> 0.22
            5 -> 0.18
            6 -> 0.15
            7 -> 0.13
            else -> 1.0 / spanCount
        }
        val gridLayoutManager =
            object : GridLayoutManager(activity, spanCount, RecyclerView.VERTICAL, false) {
                override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                    return lp?.let {
                        it.width = (widthRatio * width).toInt()
                        true
                    } ?: super.checkLayoutParams(null)
                }
            }
        binding.rvSearch.apply {
            adapter = searchAdapter
            layoutManager = gridLayoutManager
            addOnScrollListener(this@SearchFragment.scrollListener)
        }
    }

    private fun navigateToMedia(media: Media) {
        Keyboard.hide(binding.searchBar)
        val action = SearchFragmentDirections.actionSearchFragmentToFragmentMedia(media)
        findNavController().navigateSafe(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSearch.adapter = null
        _binding = null
    }
}