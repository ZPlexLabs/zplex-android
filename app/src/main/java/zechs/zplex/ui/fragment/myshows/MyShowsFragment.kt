package zechs.zplex.ui.fragment.myshows

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import zechs.zplex.R
import zechs.zplex.adapter.shared_adapters.media.MediaAdapter
import zechs.zplex.databinding.FragmentMyShowsBinding
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.ui.BaseFragment
import zechs.zplex.utils.navigateSafe


class MyShowsFragment : BaseFragment() {

    companion object {
        const val TAG = "MyShowsFragment"
    }

    private var _binding: FragmentMyShowsBinding? = null
    private val binding get() = _binding!!

    private val myShowsViewModel by activityViewModels<MyShowsViewModel>()

    private val mediaAdapter by lazy {
        MediaAdapter(
            rating = true,
            mediaOnClick = { navigateToMedia(it) }
        )
    }

    private var selectedTabIndex = 0

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
        outState.putInt("selectedTab", selectedTabIndex)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyShowsBinding.bind(view)

        setupRecyclerView()

        savedInstanceState?.let {
            val selectedTab = it.getInt("selectedTab")
            handleSelectedTab(selectedTab)
        } ?: handleSelectedTab(selectedTabIndex)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                handleTabNavigation(tab)
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

                val bottomNavView = activity?.findViewById(
                    R.id.bottomNavigationView
                ) as BottomNavigationView?

                val name = media.name ?: media.title

                Log.d(TAG, "name=$name, mediaType=${media.media_type}")

                val snackBar = Snackbar.make(
                    view, "$name removed from your library",
                    Snackbar.LENGTH_SHORT
                )
                when (media.media_type) {
                    "tv" -> {
                        val show = Show(
                            id = media.id,
                            name = media.name ?: "",
                            media_type = media.media_type,
                            poster_path = media.poster_path,
                            vote_average = media.vote_average
                        )
                        myShowsViewModel.deleteShow(show)
                        snackBar.setAction(R.string.undo) {
                            Log.d(TAG, "Undo invoked(), show=$show")
                            myShowsViewModel.saveShow(show)
                        }
                    }
                    "movie" -> {
                        val movie = Movie(
                            id = media.id,
                            title = media.title ?: "",
                            media_type = media.media_type,
                            poster_path = media.poster_path,
                            vote_average = media.vote_average
                        )
                        myShowsViewModel.deleteMovie(movie)
                        snackBar.setAction(R.string.undo) {
                            Log.d(TAG, "Undo invoked(), movie=$movie")
                            myShowsViewModel.saveMovie(movie)
                        }
                    }
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
        TransitionManager.beginDelayedTransition(
            binding.root,
        )
        selectedTabIndex = binding.tabLayout.selectedTabPosition
        when (tab!!.text) {
            resources.getString(R.string.movies) -> observeMovies()
            resources.getString(R.string.tv_shows) -> observeShows()
            else -> removeAllObservers()
        }
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
        val gridLayoutManager = object : GridLayoutManager(activity, 3) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                return lp?.let {
                    it.width = (0.30 * width).toInt()
                    true
                } ?: super.checkLayoutParams(lp)
            }
        }

        binding.rvMyShows.apply {
            adapter = mediaAdapter
            layoutManager = gridLayoutManager
        }
    }

    private fun navigateToMedia(media: Media) {
        val action = MyShowsFragmentDirections.actionMyShowsFragmentToFragmentMedia(media)
        findNavController().navigateSafe(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvMyShows.adapter = null
        _binding = null
    }

}