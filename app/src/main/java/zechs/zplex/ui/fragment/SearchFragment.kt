package zechs.zplex.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zechs.zplex.adapter.FilesAdapter
import zechs.zplex.databinding.FragmentSearchBinding
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.viewmodel.file.FileViewModel
import zechs.zplex.utils.Constants.Companion.PAGE_TOKEN
import zechs.zplex.utils.Constants.Companion.SEARCH_DELAY_AMOUNT
import zechs.zplex.utils.Resource


class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding

    private lateinit var viewModel: FileViewModel
    private lateinit var filesAdapter: FilesAdapter
    private val TAG = "SearchFragment"
    private var text = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as ZPlexActivity).viewModel
        setupRecyclerView()

        val appBarLayout = binding.appBarLayout
        appBarLayout.setPadding(
            appBarLayout.paddingLeft,
            appBarLayout.paddingTop + getStatusBarHeight() + 16,
            appBarLayout.paddingRight,
            appBarLayout.paddingBottom
        )

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
                        viewModel.getSearchList(setDriveQuery(text), "")
                    }
                }
            }
        }

        viewModel.searchList.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    TransitionManager.beginDelayedTransition(binding.root)
                    response.data?.let { filesResponse ->
                        filesAdapter.differ.submitList(filesResponse.files.toList())
                        isLastPage = filesResponse.nextPageToken == null
                        Log.d("pageToken", PAGE_TOKEN)
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(
                            context,
                            "An error occurred: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "An error occurred: $message")
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
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

    private fun hideProgressBar() {
        binding.loadingSearch.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.loadingSearch.visibility = View.VISIBLE
        isLoading = true
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as GridLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= 1

            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getSearchList(setDriveQuery(text), PAGE_TOKEN)
                isScrolling = false
                Log.d(tag, "Paginating")
            } else {
                Log.d(tag, "Not paginating")
                binding.rvSearch.setPadding(0, 0, 0, 0)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setupRecyclerView() {
        val nestedScroll = binding.nestedScrollView
        nestedScroll.viewTreeObserver.addOnScrollChangedListener {
            val view = nestedScroll.getChildAt(nestedScroll.childCount - 1) as View
            val diff: Int = view.bottom - (nestedScroll.height + nestedScroll.scrollY)
            if (diff == 0 && !isLastPage) {
                viewModel.getSearchList(setDriveQuery(text), PAGE_TOKEN)
            }
        }

        filesAdapter = FilesAdapter()
        binding.rvSearch.apply {
            adapter = filesAdapter
            layoutManager = GridLayoutManager(activity, 3)
            //addOnScrollListener(this@SearchFragment.scrollListener)
            isNestedScrollingEnabled = false
        }

        filesAdapter.setOnItemClickListener {
            try {
                val seriesId = (it.name.split(" - ").toTypedArray()[0]).toInt()
                val name = it.name.split(" - ").toTypedArray()[1]
                val type = it.name.split(" - ").toTypedArray()[2]

                val action = SearchFragmentDirections.actionSearchFragmentToAboutFragment(
                    it,
                    seriesId,
                    type,
                    name,
                )
                findNavController().navigate(action)
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "TVDB id not found", Toast.LENGTH_LONG).show()
            }
        }
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