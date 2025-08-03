package zechs.zplex.ui.myshows

import android.content.res.ColorStateList
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import zechs.zplex.R
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.entities.Show
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.FragmentMyShowsBinding
import zechs.zplex.ui.shared_adapters.media.MediaAdapter
import zechs.zplex.utils.MaterialMotionInterpolator
import zechs.zplex.utils.ext.navigateSafe


class MyShowsFragment : Fragment() {

    companion object {
        const val TAG = "MyShowsFragment"
    }

    private var _binding: FragmentMyShowsBinding? = null
    private val binding get() = _binding!!

    private val myShowsViewModel by activityViewModels<MyShowsViewModel>()
    private var layoutManager: RecyclerView.LayoutManager? = null

    private val mediaAdapter by lazy {
        MediaAdapter(
            rating = true,
            mediaOnClick = { navigateToMedia(it) }
        )
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

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val media = mediaAdapter.currentList[position]

                val bottomNavView: BottomNavigationView? =
                    activity?.findViewById(R.id.bottomNavigationView)

                val name = media.name ?: media.title

                Log.d(TAG, "name=$name, mediaType=${media.media_type}")

                val snackBar = Snackbar.make(
                    view, "$name removed from your library",
                    Snackbar.LENGTH_SHORT
                )
                when (media.media_type) {
                    MediaType.tv -> {
                        val show = Show(
                            id = media.id,
                            name = media.name ?: "",
                            media_type = media.media_type.name,
                            poster_path = media.poster_path,
                            vote_average = media.vote_average,
                            fileId = media.fileId,
                            modifiedTime = media.modifiedTime
                        )
                        myShowsViewModel.deleteShow(media.id)
                        snackBar.setAction(R.string.undo) {
                            Log.d(TAG, "Undo invoked(), show=$show")
                            myShowsViewModel.saveShow(show)
                        }
                    }
                    MediaType.movie -> {
                        val movie = Movie(
                            id = media.id,
                            title = media.title ?: "",
                            media_type = media.media_type.name,
                            poster_path = media.poster_path,
                            vote_average = media.vote_average,
                            fileId = media.fileId,
                            modifiedTime = media.modifiedTime
                        )
                        myShowsViewModel.deleteMovie(movie.id)
                        snackBar.setAction(R.string.undo) {
                            Log.d(TAG, "Undo invoked(), movie=$movie")
                            myShowsViewModel.saveMovie(movie)
                        }
                    }
                    else -> {}
                }

                bottomNavView?.let {
                    snackBar.anchorView = it
                }
                val accentColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorAccent
                    )
                )

                snackBar.setActionTextColor(accentColor)
                snackBar.show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvMyShows)
    }

    private fun handleSelectedTab(selectedTab: Int) {
        binding.tabLayout.getTabAt(selectedTab)!!.select()
        when (selectedTab) {
            0 -> observeMovies()
            1 -> observeShows()
            else -> removeAllObservers()
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
                    else -> removeAllObservers()
                }

                // Prep new list position
                recyclerView.translationX = direction * width
                recyclerView.alpha = 0f
            }
            .start()
    }

    private fun observeMovies() {
        myShowsViewModel.movies.observe(viewLifecycleOwner) { media ->
            handleLibrary(media)
            mediaAdapter.submitList(media.map { it.toMedia() })
        }
        myShowsViewModel.shows.removeObservers(viewLifecycleOwner)
    }

    private fun observeShows() {
        myShowsViewModel.shows.observe(viewLifecycleOwner) { media ->
            handleLibrary(media)
            mediaAdapter.submitList(media.map { it.toMedia() })
        }

        myShowsViewModel.movies.removeObservers(viewLifecycleOwner)
    }

    private fun removeAllObservers() {
        myShowsViewModel.shows.removeObservers(viewLifecycleOwner)
        myShowsViewModel.movies.removeObservers(viewLifecycleOwner)
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

        mediaAdapter.registerAdapterDataObserver(adapterObserver)

        binding.rvMyShows.apply {
            adapter = mediaAdapter
            layoutManager = gridLayoutManager
        }
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