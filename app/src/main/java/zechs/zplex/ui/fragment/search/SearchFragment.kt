package zechs.zplex.ui.fragment.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zechs.zplex.adapter.shared_adapters.media.MediaAdapter
import zechs.zplex.databinding.FragmentSearchBinding
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.ui.BaseFragment
import zechs.zplex.ui.activity.main.MainActivity
import zechs.zplex.utils.Constants.SEARCH_DELAY_AMOUNT
import zechs.zplex.utils.Keyboard
import zechs.zplex.utils.Resource
import zechs.zplex.utils.navigateSafe
import zechs.zplex.utils.setupClearButtonWithAction


class SearchFragment : BaseFragment() {

    companion object {
        const val TAG = "SearchFragment"
    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchViewModel: SearchViewModel

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

        searchViewModel = (activity as MainActivity).searchViewModel

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
                            it.media_type == "tv" || it.media_type == "movie"
                                    && it.poster_path != null
                        }

                        lifecycleScope.launch {
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
        binding.rvSearch.apply {
            adapter = searchAdapter
            layoutManager = GridLayoutManager(activity, 3)
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