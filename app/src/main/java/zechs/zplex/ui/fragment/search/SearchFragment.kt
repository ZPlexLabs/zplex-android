package zechs.zplex.ui.fragment.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.adapter.SearchAdapter
import zechs.zplex.databinding.FragmentSearchBinding
import zechs.zplex.models.dataclass.MediaArgs
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.utils.Constants.SEARCH_DELAY_AMOUNT
import zechs.zplex.utils.Resource


class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchViewModel: SearchViewModel

    private val searchAdapter by lazy { SearchAdapter() }
    private val thisTag = "SearchFragment"
    private var queryText = ""
    private var isLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, true
        ).apply {
            duration = 500L
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        searchViewModel = (activity as ZPlexActivity).searchViewModel
        setupRecyclerView()

        var job: Job? = null
        binding.searchBox.apply {
            this.requestFocus()
            showKeyboard()
            editText?.text = Editable.Factory.getInstance().newEditable(queryText)
            editText?.addTextChangedListener { editable ->
                binding.rvSearch.isInvisible = true
                job?.cancel()
                job = MainScope().launch {
                    delay(SEARCH_DELAY_AMOUNT)
                    editable?.let {
                        queryText = it.toString()
                        if (queryText.isNotEmpty()) {
                            searchViewModel.getSearchList(queryText)
                        }
                    }
                }
            }
        }

        searchViewModel.searchList.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    isLoading = false
                    TransitionManager.beginDelayedTransition(binding.root)
                    binding.apply {
                        pbSearch.isInvisible = true
                        rvSearch.isInvisible = queryText.isEmpty()
                    }
                    response.data?.let { searchResponse ->
                        if (searchResponse.results.isEmpty()) {
                            Toast.makeText(
                                context, "Nothing found", Toast.LENGTH_SHORT
                            ).show()
                        }
                        val searchList = searchResponse.results.filter {
                            it.media_type == "tv" || it.media_type == "movie"
                        }
                        searchAdapter.differ.submitList(searchList.toList())
                    }
                }
                is Resource.Error -> {
                    isLoading = false
                    response.message?.let { message ->
                        Toast.makeText(
                            context, "An error occurred: $message", Toast.LENGTH_SHORT
                        ).show()
                        Log.e(thisTag, "An error occurred: $message")
                    }
                    binding.pbSearch.isInvisible = true
                }
                is Resource.Loading -> {
                    isLoading = true
                    binding.pbSearch.isVisible = true
                    binding.apply {
                        rvSearch.isVisible = false
                    }
                }
            }
        })

    }


    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0) {
                val layoutManager = binding.rvSearch.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.findLastCompletelyVisibleItemPosition() + 1
                val itemCount = layoutManager.itemCount - 6

                Log.d(
                    "onScrolled",
                    "visibleItemCount=$visibleItemCount, itemCount=$itemCount, isLoading=$isLoading"
                )

//                if (visibleItemCount == itemCount && !isLoading && !isLastPage) {
//                    searchViewModel.getSearchList(setDriveQuery(queryText), PAGE_TOKEN)
//                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvSearch.apply {
            adapter = searchAdapter
            layoutManager = GridLayoutManager(activity, 3)
            addOnScrollListener(this@SearchFragment.scrollListener)
        }

        searchAdapter.setOnItemClickListener {
            hideKeyboard()
            val action = SearchFragmentDirections.actionSearchFragmentToFragmentMedia(
                MediaArgs(
                    it.id,
                    it.media_type ?: "none", it
                )
            )
            findNavController().navigate(action)
        }
    }

    private fun hideKeyboard() {
        activity?.currentFocus.let { view ->
            val imm = context?.getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }

    private fun showKeyboard() {
        activity?.currentFocus.let { view ->
            val imm = context?.getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            rvSearch.adapter = null
        }
        _binding = null
    }
}