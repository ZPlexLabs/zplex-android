package zechs.zplex.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import zechs.zplex.R
import zechs.zplex.core.navigation.navigateSafe
import zechs.zplex.data.model.entities.WatchedShow
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.feature_home.ui.HomeFragmentDirections
import zechs.zplex.ui.home.adapter.HomeClickListener
import zechs.zplex.ui.home.adapter.HomeDataAdapter
import zechs.zplex.ui.home.adapter.HomeDataModel
import zechs.zplex.ui.home.adapter.MenuType
import zechs.zplex.ui.home.adapter.watched.WatchedDataModel
import zechs.zplex.ui.shared_viewmodels.SeasonViewModel
import zechs.zplex.zplex_api.data.remote.api.movies.LatestMovie
import zechs.zplex.zplex_api.data.remote.api.suggestions.SuggestionMediaItem
import zechs.zplex.zplex_api.data.remote.api.tvshows.LatestTvShow


class HomeFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
    }

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val seasonViewModel by activityViewModels<SeasonViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(
            inflater, container, /* attachToParent */false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentListBinding.bind(view)

        binding.toolbar.apply {
            navigationIcon = null
            isTitleCentered = false
            setTitleTextAppearance(context, R.style.homeTitleTextAppearance)
            title = resources.getString(R.string.app_name)
        }

        setupRecyclerView()
        homeViewModel.watchedMedia.observe(viewLifecycleOwner) { watchedList ->
            watchedList?.let {
                val sortedList = it.sortedByDescending { watchedData ->
                    when (watchedData) {
                        is WatchedDataModel.Show -> watchedData.show.createdAt
                        is WatchedDataModel.Movie -> watchedData.movie.createdAt
                    }
                }
                setupWatchedList(sortedList)
            }
        }
    }

    private fun homeMediaError(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private val homeDataAdapter by lazy {
        HomeDataAdapter(homeClickListener)
    }

    private val homeClickListener = object : HomeClickListener {
        //        override fun onClickMedia(media: Media) {
//            navigateToMedia(media)
//        }
//
//        override fun onClickWatched(watched: WatchedDataModel) {
//            when (watched) {
//                is WatchedDataModel.Movie -> navigateToMedia(watched.movie.toMedia())
//                is WatchedDataModel.Show -> navigateToSeason(watched.show)
//            }
//        }
//
//        override fun onLongClickWatched(watched: WatchedDataModel) {
//            MaterialAlertDialogBuilder(requireContext())
//                .setTitle(getString(R.string.remove_from_watched))
//                .setPositiveButton(getString(R.string.yes)) { _, _ ->
//                    homeViewModel.removeWatchedMedia(watched)
//                }
//                .setNegativeButton(getString(R.string.no)) { _, _ -> }
//                .show()
//        }
        override fun onClickMenu(type: MenuType) {
            TODO("Not yet implemented")
        }

        override fun onClickLatestShow(show: LatestTvShow) {
            TODO("Not yet implemented")
        }

        override fun onClickLatestMovie(movie: LatestMovie) {
            TODO("Not yet implemented")
        }

        override fun onClickSuggestionItem(suggestion: SuggestionMediaItem) {
            TODO("Not yet implemented")
        }
    }

    private fun setupWatchedList(watchedList: List<WatchedDataModel>) {
        val currentList = mutableListOf<HomeDataModel>()
        if (watchedList.isNotEmpty()) {
            currentList.add(HomeDataModel.Header(getString(R.string.continue_watching)))
            currentList.add(HomeDataModel.Watched(watchedList))
        }
        currentList.add(HomeDataModel.Header("\uD83D\uDEA7 This screen is under construction \uD83D\uDEA7"))
        homeDataAdapter.submitList(currentList)
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = homeDataAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
        }
    }

    private fun navigateToSeason(show: WatchedShow) {
        seasonViewModel.setShowSeason(
            tmdbId = show.tmdbId,
            seasonName = null,
            seasonNumber = show.seasonNumber,
            showName = show.name,
            seasonPosterPath = null,
            showPoster = show.posterPath,
            seasons = listOf()
        )
        findNavController().navigateSafe(R.id.action_homeFragment_to_episodesListFragment)
    }

    private fun navigateToMedia(media: Media) {
//        HomeFragmentDirections
//            .actionHomeFragmentToFragmentMedia(media)
//            .also {
//                findNavController().navigateSafe(it)
//            }
    }

    private fun isLoading(hide: Boolean) {
        binding.loading.isVisible = hide
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}