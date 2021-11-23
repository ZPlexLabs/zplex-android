package zechs.zplex.ui.fragment.search

import android.os.Bundle
import android.text.Editable
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
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
import zechs.zplex.utils.Resource


class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchViewModel: SearchViewModel
    private val argsModel: ArgsViewModel by activityViewModels()

    private lateinit var filesAdapter: FilesAdapter
    private val thisTag = "SearchFragment"
    private var text = ""
    private var isLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        searchViewModel = (activity as ZPlexActivity).searchViewModel
        setupRecyclerView()

        var job: Job? = null
        binding.searchBox.apply {
            editText?.text = Editable.Factory.getInstance().newEditable(text)
            editText?.addTextChangedListener { editable ->
                job?.cancel()
                job = MainScope().launch {
                    delay(SEARCH_DELAY_AMOUNT)
                    editable?.let {
                        PAGE_TOKEN = ""
                        text = editable.toString()
                        searchViewModel.getSearchList(setDriveQuery(text), "")
                    }
                }
            }
        }

        searchViewModel.searchList.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    isLoading = false
                    TransitionManager.beginDelayedTransition(binding.root)
                    response.data?.let { filesResponse ->
                        if (filesResponse.files.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Nothing found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        filesAdapter.differ.submitList(filesResponse.files.toList())
                        Log.d("pageToken", PAGE_TOKEN)
                    }
                }
                is Resource.Error -> {
                    isLoading = false
                    response.message?.let { message ->
                        Toast.makeText(
                            context,
                            "An error occurred: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(thisTag, "An error occurred: $message")
                    }
                }
                is Resource.Loading -> {
                    isLoading = true
                }
            }
        })
    }

    private fun setDriveQuery(query: String): String {
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
                if (visibleItemCount == layoutManager.itemCount && !isLoading) {
                    searchViewModel.getSearchList(setDriveQuery(text), PAGE_TOKEN)
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
            try {
                val seriesId = (it.name.split(" - ").toTypedArray()[0]).toInt()
                val name = it.name.split(" - ").toTypedArray()[1]
                val type = it.name.split(" - ").toTypedArray()[2]

                argsModel.setArg(
                    Args(
                        file = it,
                        mediaId = seriesId,
                        type = type,
                        name = name
                    )
                )

                findNavController().navigate(R.id.action_searchFragment_to_aboutFragment)
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "TVDB id not found", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSearch.adapter = null
        _binding = null
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}