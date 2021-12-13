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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.adapter.FilesAdapter
import zechs.zplex.databinding.FragmentSearchBinding
import zechs.zplex.models.Args
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.ArgsViewModel
import zechs.zplex.utils.Constants.PAGE_TOKEN
import zechs.zplex.utils.Constants.SEARCH_DELAY_AMOUNT
import zechs.zplex.utils.Constants.isLastPage
import zechs.zplex.utils.Constants.regexShow
import zechs.zplex.utils.Resource


class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchViewModel: SearchViewModel
    private val argsModel: ArgsViewModel by activityViewModels()

    private lateinit var filesAdapter: FilesAdapter
    private val thisTag = "SearchFragment"
    private var queryText = ""
    private var isLoading = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        searchViewModel = (activity as ZPlexActivity).searchViewModel
        setupRecyclerView()

        var job: Job? = null
        binding.searchBox.apply {
            editText?.text = Editable.Factory.getInstance().newEditable(queryText)
            editText?.addTextChangedListener { editable ->
                job?.cancel()
                job = MainScope().launch {
                    delay(SEARCH_DELAY_AMOUNT)
                    editable?.let {
                        PAGE_TOKEN = ""
                        queryText = editable.toString()
                        searchViewModel.getSearchList(setDriveQuery(queryText), "")
                    }
                }
            }
        }

        searchViewModel.searchList.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    isLoading = false
                    TransitionManager.beginDelayedTransition(binding.root)
                    binding.loadingSearch.isInvisible = true
                    response.data?.let { filesResponse ->
                        if (filesResponse.files.isEmpty()) {
                            Toast.makeText(
                                context, "Nothing found", Toast.LENGTH_SHORT
                            ).show()
                        }
                        filesAdapter.differ.submitList(filesResponse.files.toList())
                        isLastPage = filesResponse.nextPageToken.isNullOrEmpty()
                        Log.d("pageToken", PAGE_TOKEN)
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
                    binding.loadingSearch.isInvisible = true
                }
                is Resource.Loading -> {
                    isLoading = true
                    binding.loadingSearch.isVisible = true
                }
            }
        })
    }

    private fun setDriveQuery(query: String): String {
        queryText = query
        return if (query == "") {
            "(name contains 'TV' or name contains 'Movie') and '0AASFDMjRqUB0Uk9PVA' in parents and trashed = false"
        } else {
            "name contains '$query' and (name contains 'TV' or name contains 'Movie') and '0AASFDMjRqUB0Uk9PVA' in parents and trashed = false"
        }
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

                if (visibleItemCount == itemCount && !isLoading && !isLastPage) {
                    searchViewModel.getSearchList(setDriveQuery(queryText), PAGE_TOKEN)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        filesAdapter = FilesAdapter()
        binding.rvSearch.apply {
            adapter = filesAdapter
            layoutManager = GridLayoutManager(activity, 3)
            isNestedScrollingEnabled = false
            addOnScrollListener(this@SearchFragment.scrollListener)
        }

        filesAdapter.setOnItemClickListener {
            hideKeyboard()
            val nameSplit = regexShow.toRegex().find(it.name)?.destructured?.toList()

            if (nameSplit != null) {
                val mediaId = nameSplit[0]
                val mediaName = nameSplit[2]
                val mediaType = nameSplit[4]

                argsModel.setArg(
                    Args(
                        file = it,
                        mediaId = mediaId.toInt(),
                        type = mediaType,
                        name = mediaName
                    )
                )
                findNavController().navigate(R.id.action_searchFragment_to_aboutFragment)
            }
        }
    }

    private fun hideKeyboard() {
        activity?.currentFocus.let { view ->
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSearch.adapter = null
        _binding = null
    }
}