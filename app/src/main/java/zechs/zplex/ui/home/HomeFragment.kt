package zechs.zplex.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.data.model.entities.WatchedShow
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.ui.home.adapter.HomeClickListener
import zechs.zplex.ui.home.adapter.HomeDataAdapter
import zechs.zplex.ui.home.adapter.HomeDataModel
import zechs.zplex.ui.home.adapter.watched.WatchedDataModel
import zechs.zplex.ui.shared_viewmodels.SeasonViewModel
import zechs.zplex.utils.ext.navigateSafe
import zechs.zplex.utils.state.Resource


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
        setupTrendingObserver()
    }

    private fun setupTrendingObserver() {
        homeViewModel.homeMedia.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    Log.d(TAG, "Success")
                    isLoading(false)
                    homeMediaSuccess(response.data!!)
                }
                is Resource.Error -> {
                    isLoading(false)
                    Log.d(TAG, "Error: ${response.message}")
                    homeMediaError(response.message!!)
                }
                is Resource.Loading -> {
                    isLoading(true)
                    Log.d(TAG, "isLoading")
                }
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
        override fun onClickMedia(media: Media) {
            navigateToMedia(media)
        }

        override fun onClickWatched(watched: WatchedDataModel) {
            when (watched) {
                is WatchedDataModel.Movie -> navigateToMedia(watched.movie.toMedia())
                is WatchedDataModel.Show -> navigateToSeason(watched.show)
            }
        }

        override fun onLongClickWatched(watched: WatchedDataModel) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.remove_from_watched))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    homeViewModel.removeWatchedMedia(watched)
                }
                .setNegativeButton(getString(R.string.no)) { _, _ -> }
                .show()
        }
    }

    private fun homeMediaSuccess(listResponse: List<HomeDataModel>) {
        homeDataAdapter.submitList(listResponse)

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

    private fun setupWatchedList(
        watchedList: List<WatchedDataModel>
    ) = viewLifecycleOwner.lifecycleScope.launch {
        val currentList = homeDataAdapter.currentList.toMutableList()
        Log.d(TAG, "WatchedDataModel=$watchedList")
        Log.d(TAG, "currentListSize=${homeDataAdapter.itemCount}")
        Log.d(TAG, "currentList.size=${currentList.size}")
        currentList.forEachIndexed { i, a ->
            Log.d(TAG, "currentList[$i]=$a")
        }
        when (homeDataAdapter.itemCount) {
            5 -> {
                if (watchedList.isNotEmpty()) {
                    currentList.add(1, HomeDataModel.Header("Continue Watching"))
                    currentList.add(2, HomeDataModel.Watched(watchedList))
                }
            }
            7 -> {
                currentList.removeAt(1)
                currentList.removeAt(1)
                if (watchedList.isNotEmpty()) {
                    currentList.add(1, HomeDataModel.Header("Continue Watching"))
                    currentList.add(2, HomeDataModel.Watched(watchedList))
                }
            }
            else -> {}
        }
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
            showPoster = show.posterPath
        )
        findNavController().navigateSafe(R.id.action_homeFragment_to_episodesListFragment)
    }

    private fun navigateToMedia(media: Media) {
        HomeFragmentDirections
            .actionHomeFragmentToFragmentMedia(media)
            .also {
                findNavController().navigateSafe(it)
            }
    }

    private fun isLoading(hide: Boolean) {
        binding.loading.isVisible = hide
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}