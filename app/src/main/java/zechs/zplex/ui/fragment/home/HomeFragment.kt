package zechs.zplex.ui.fragment.home

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.adapter.home.HomeClickListener
import zechs.zplex.adapter.home.HomeDataAdapter
import zechs.zplex.adapter.home.HomeDataModel
import zechs.zplex.adapter.watched.WatchedDataModel
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.models.dataclass.WatchedShow
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.ui.BaseFragment
import zechs.zplex.ui.fragment.shared_viewmodels.SeasonViewModel
import zechs.zplex.utils.Resource
import zechs.zplex.utils.navigateSafe


class HomeFragment : BaseFragment(), HomeClickListener {

    private val homeDataAdapter by lazy {
        HomeDataAdapter(
            context = requireContext(),
            homeClickListener = this@HomeFragment
        )
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
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    response.data?.let { trendingSuccess(it) }
                }
                is Resource.Error -> {
                    isLoading(false)
                    Log.d(TAG, "Error: ${response.message}")
                }
                is Resource.Loading -> {
                    isLoading(true)
                    Log.d(TAG, "isLoading")
                }
            }
        }
    }


    private fun trendingSuccess(listResponse: List<HomeDataModel>) {
        viewLifecycleOwner.lifecycleScope.launch {
            homeDataAdapter.differ.submitList(listResponse)
        }

        homeViewModel.watchedMedia.observe(viewLifecycleOwner) {
            it?.let {
                val currentList = homeDataAdapter.differ.currentList.toMutableList()
                println("WatchedDataModel=$it")
                println("currentListSize=${homeDataAdapter.itemCount}")
                println("currentList.size=${currentList.size}")
                currentList.forEachIndexed { i, a ->
                    println("$i. $a")
                }
                when (homeDataAdapter.itemCount) {
                    5 -> {
                        if (it.isNotEmpty()) {
                            currentList.add(1, HomeDataModel.Header("Continue Watching"))
                            currentList.add(2, HomeDataModel.Watched(it))
                        }
                    }
                    7 -> {
                        currentList.removeAt(1)
                        currentList.removeAt(1)
                        if (it.isNotEmpty()) {
                            currentList.add(1, HomeDataModel.Header("Continue Watching"))
                            currentList.add(2, HomeDataModel.Watched(it))
                        }
                    }
                    else -> {}
                }
                homeDataAdapter.differ.submitList(currentList)
            }
        }
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
        val action = HomeFragmentDirections.actionHomeFragmentToFragmentMedia(media)
        findNavController().navigateSafe(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isLoading(hide: Boolean) {
        binding.loading.isVisible = hide
    }

    companion object {
        const val TAG = "HomeFragment"
    }

    override fun onClickMedia(media: Media) {
        navigateToMedia(media)
    }

    override fun onClickWatched(watched: WatchedDataModel) {
        when (watched) {
            is WatchedDataModel.Movie -> navigateToMedia(watched.movie.toMedia())
            is WatchedDataModel.Show -> navigateToSeason(watched.show)
        }
    }

    override fun setImageResource(image: Drawable) {
        //TODO: Implement palette api using image

    }

    override fun setRatingBarView(ratingBar: RatingBar) {
        //TODO: Implement Dynamic color
    }

}