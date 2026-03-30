package zechs.zplex.ui.myshows

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.core.navigation.navigateSafe
import zechs.zplex.core.ui.animation.MaterialMotionInterpolator
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.FragmentMyShowsBinding
import zechs.zplex.ui.myshows.adapter.MediaListItemAdapter
import zechs.zplex.utils.UiResult
import zechs.zplex.zplex_api.data.remote.api.MediaListItem


class MyShowsFragment : Fragment() {

    companion object {
        const val TAG = "MyShowsFragment"
    }

    private var _binding: FragmentMyShowsBinding? = null
    private val binding get() = _binding!!

    private val myShowsViewModel by activityViewModels<MyShowsViewModel>()
    private var layoutManager: RecyclerView.LayoutManager? = null

    private val mediaAdapter by lazy {
        MediaListItemAdapter(
            onClick = {
                Snackbar.make(binding.root, "Clicked on ${it.title}", Snackbar.LENGTH_SHORT)
                    .show()
            }
        )
    }

    class GridSpacingDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            outRect.set(spacing, spacing, spacing, spacing)
        }
    }

    private var selectedTabIndex = 0
    private val tabScrollStates = mutableMapOf<Int, Parcelable?>()
    private var pendingRestoreState: Parcelable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyShowsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Always update the current tab’s scroll state before saving
        tabScrollStates[selectedTabIndex] = layoutManager?.onSaveInstanceState()

        // Save each tab’s scroll state under its own key
        tabScrollStates.forEach { (index, state) ->
            outState.putParcelable("recycler_layout_state_$index", state)
        }

        outState.putInt("selectedTab", selectedTabIndex)

        Log.d(TAG, "Saving tabScrollStates: $tabScrollStates")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyShowsBinding.bind(view)
        layoutManager = binding.rvMyShows.layoutManager

        setupRecyclerView()

        savedInstanceState?.let {
            // Restore scroll states for all tabs
            for (tabIndex in 0..1) { // adjust range for your tab count
                val state = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    savedInstanceState.getParcelable(
                        "recycler_layout_state_$tabIndex",
                        Parcelable::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    savedInstanceState.getParcelable("recycler_layout_state_$tabIndex")
                }
                tabScrollStates[tabIndex] = state
            }

            // Restore selected tab
            selectedTabIndex = savedInstanceState.getInt("selectedTab", 0)
            handleSelectedTab(selectedTabIndex)

            // Restore scroll state for that tab
            val restoredState = tabScrollStates[selectedTabIndex]
            binding.rvMyShows.layoutManager?.onRestoreInstanceState(restoredState)
            Log.d(TAG, "Restored state for tab $selectedTabIndex: $restoredState")
        } ?: run {
            Log.d(TAG, "No savedInstanceState, using default tab: $selectedTabIndex")
            handleSelectedTab(selectedTabIndex)
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val newTabIndex = tab?.position ?: return

                // Save old tab scroll
                val currentState = binding.rvMyShows.layoutManager?.onSaveInstanceState()
                tabScrollStates[selectedTabIndex] = currentState
                Log.d(TAG, "Saved scroll for tab $selectedTabIndex: $currentState")

                // Switch tab index
                selectedTabIndex = newTabIndex
                handleTabNavigation(tab)

                // Instead of restoring immediately, defer it:
                pendingRestoreState = tabScrollStates[newTabIndex]
                Log.d(TAG, "Queued restore for tab $newTabIndex: $pendingRestoreState")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun handleSelectedTab(selectedTab: Int) {
        binding.tabLayout.getTabAt(selectedTab)!!.select()
        when (selectedTab) {
            0 -> observeMovies()
            1 -> observeShows()
        }
    }

    private fun handleTabNavigation(tab: TabLayout.Tab?) {
        val recyclerView = binding.rvMyShows

        val newTabText = tab?.text ?: return
        val direction = if (newTabText == resources.getString(R.string.movies)) -1 else 1
        val width = recyclerView.width.toFloat()

        recyclerView.animate()
            .translationX(direction * -width)
            .alpha(0f)
            .setInterpolator(MaterialMotionInterpolator.getEmphasizedAccelerateInterpolator())
            .setDuration(150)
            .withEndAction {
                // Switch data
                selectedTabIndex = binding.tabLayout.selectedTabPosition
                when (newTabText) {
                    resources.getString(R.string.movies) -> observeMovies()
                    resources.getString(R.string.tv_shows) -> observeShows()
                }

                // Prep new list position
                recyclerView.translationX = direction * width
                recyclerView.alpha = 0f
            }
            .start()
    }

    private fun observeMovies() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                myShowsViewModel.movies.collect { state ->
                    renderMovies(state)
                }
            }
        }
    }

    private fun observeShows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                myShowsViewModel.tvShows.collect { state ->
                    renderTvShows(state)
                }
            }
        }
    }

    private fun renderTvShows(state: UiResult<MediaListItem>) {
        renderMedia(
            state = state,
            onInitialLoad = { myShowsViewModel.loadTvShows() },
            onRetry = { myShowsViewModel.retryTvShows() },
            onSuccess = { handleLibrary(it) }
        )
    }

    private fun renderMovies(state: UiResult<MediaListItem>) {
        renderMedia(
            state = state,
            onInitialLoad = { myShowsViewModel.loadMovies() },
            onRetry = { myShowsViewModel.retryMovies() },
            onSuccess = { handleLibrary(it) }
        )
    }

    private fun renderMedia(
        state: UiResult<MediaListItem>,
        onInitialLoad: () -> Unit,
        onRetry: () -> Unit,
        onSuccess: (List<MediaListItem>) -> Unit = {}
    ) {
        when (state) {

            is UiResult.Idle -> {
                binding.progressBar.isVisible = false
                binding.rvMyShows.isVisible = false
                binding.errorView.root.isVisible = false

                onInitialLoad()
            }

            is UiResult.Loading -> {
                if (state.data.isEmpty()) {
                    // First load
                    binding.progressBar.isVisible = true
                    binding.rvMyShows.isVisible = false
                } else {
                    // Pagination loading
                    binding.progressBar.isVisible = false
                    binding.rvMyShows.isVisible = true

                    showPaginationLoader(true)
                    mediaAdapter.submitList(state.data)
                }

                binding.errorView.root.isVisible = false
            }

            is UiResult.Success -> {
                binding.progressBar.isVisible = false
                binding.rvMyShows.isVisible = true
                binding.errorView.root.isVisible = false

                showPaginationLoader(false)

                onSuccess(state.data)
                mediaAdapter.submitList(state.data)
            }

            is UiResult.Error -> {
                binding.progressBar.isVisible = false

                if (state.data.isEmpty()) {
                    // Full screen error
                    binding.rvMyShows.isVisible = false
                    binding.errorView.root.isVisible = true
                    binding.errorView.errorTxt.text = state.message

                    binding.errorView.retryBtn.setOnClickListener {
                        onRetry()
                    }
                } else {
                    // Pagination error
                    binding.rvMyShows.isVisible = true
                    binding.errorView.root.isVisible = false

                    mediaAdapter.submitList(state.data)
                    showPaginationLoader(false)

                    Snackbar.make(binding.rvMyShows, state.message, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry) { onRetry() }
                        .show()
                }
            }
        }
    }

    private fun showPaginationLoader(show: Boolean) {
        binding.progressBarPagination.isGone = !show
    }

    private fun handleLibrary(media: List<*>?) {
        val noShows = ContextCompat.getDrawable(requireContext(), R.drawable.ic_no_shows_24)
        val isEmpty = media?.isEmpty() ?: true
        binding.apply {
            rvMyShows.isGone = isEmpty
            errorView.apply {
                root.isVisible = isEmpty
                retryBtn.isVisible = false
                errorTxt.text = getString(R.string.your_library_is_empty)
                errorIcon.setImageDrawable(noShows)
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

        val onScrollPaginationListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val total = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (lastVisible >= total - spanCount) {
                    val selectedTabPosition = binding.tabLayout.selectedTabPosition
                    when (selectedTabPosition) {
                        0 -> myShowsViewModel.loadMovies()
                        1 -> myShowsViewModel.loadTvShows()
                    }
                }
            }
        }
        binding.rvMyShows.apply {
            adapter = mediaAdapter
            layoutManager = gridLayoutManager
            addOnScrollListener(onScrollPaginationListener)
            addItemDecoration(GridSpacingDecoration(8))
        }

        mediaAdapter.registerAdapterDataObserver(adapterObserver)
    }

    private fun navigateToMedia(media: Media) {
        val action = MyShowsFragmentDirections.actionMyShowsFragmentToFragmentMedia(media)
        findNavController().navigateSafe(action)
    }

    override fun onPause() {
        super.onPause()

        // Always update the current tab’s scroll state when fragment pauses.
        val currentState = binding.rvMyShows.layoutManager?.onSaveInstanceState()
        tabScrollStates[selectedTabIndex] = currentState
        Log.d(TAG, "onPause -> Saved scroll for tab $selectedTabIndex: $currentState")
    }

    private val adapterObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            restorePendingScrollIfAny()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            restorePendingScrollIfAny()
        }
    }

    private fun restorePendingScrollIfAny() {
        if (pendingRestoreState != null) {
            binding.rvMyShows.layoutManager?.onRestoreInstanceState(pendingRestoreState)
            Log.d(TAG, "Actually restored scroll for tab $selectedTabIndex: $pendingRestoreState")
            pendingRestoreState = null
        }
        binding.rvMyShows.post {
            binding.rvMyShows.animate()
                .setInterpolator(MaterialMotionInterpolator.getEmphasizedDecelerateInterpolator())
                .setStartDelay(100)
                .translationX(0f)
                .alpha(1f)
                .setDuration(150)
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaAdapter.unregisterAdapterDataObserver(adapterObserver)
        binding.rvMyShows.adapter = null
        layoutManager = null
        _binding = null
    }

}