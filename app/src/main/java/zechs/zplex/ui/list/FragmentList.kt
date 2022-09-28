package zechs.zplex.ui.list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.data.model.tmdb.entities.Cast
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.data.model.tmdb.entities.Season
import zechs.zplex.data.model.tmdb.entities.Video
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.ui.list.adapter.ListClickListener
import zechs.zplex.ui.list.adapter.ListDataAdapter
import zechs.zplex.ui.list.adapter.ListDataModel
import zechs.zplex.ui.shared_viewmodels.SeasonViewModel
import zechs.zplex.utils.ext.navigateSafe

class FragmentList : Fragment(), ListClickListener {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val seasonViewModel by activityViewModels<SeasonViewModel>()
    private val listViewModel by activityViewModels<ListViewModel>()

    private val listAdapter by lazy {
        ListDataAdapter(
            listClickListener = this@FragmentList
        )
    }

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
        _binding = FragmentListBinding.bind(view)

        setupRecyclerView()
        setupListsObserver()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun setupListsObserver() {
        listViewModel.listArgs.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                when (it) {
                    is ListDataModel.Seasons -> handleSeason(it)
                    is ListDataModel.Casts -> handleCasts(it)
                    is ListDataModel.Media -> handleMedia(it)
                    is ListDataModel.Videos -> handleVideo(it)
                }
            }
        }
    }

    private fun handleSeason(it: ListDataModel.Seasons) {
        binding.toolbar.apply {
            title = "Seasons"
            subtitle = it.showName
        }

        lifecycleScope.launch {
            listAdapter.submitList(listOf(it))
        }
    }

    private fun handleCasts(it: ListDataModel.Casts) {
        binding.toolbar.apply {
            title = "Casts"
            isTitleCentered = false
        }

        listAdapter.submitList(listOf(it))
    }

    private fun handleMedia(it: ListDataModel.Media) {
        binding.toolbar.apply {
            title = it.heading
            isTitleCentered = false
        }

        listAdapter.submitList(listOf(it))
    }

    private fun handleVideo(it: ListDataModel.Videos) {
        binding.toolbar.apply {
            title = "More videos"
            isTitleCentered = false
        }

        listAdapter.submitList(listOf(it))
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
        }
    }

    override fun onClickSeason(season: Season) {
        listViewModel.listArgs.observe(viewLifecycleOwner) { event ->
            when (val it = event.peekContent()) {
                is ListDataModel.Seasons -> {
                    navigateToSeason(
                        tmdbId = it.tmdbId,
                        seasonName = season.name,
                        seasonNumber = season.season_number,
                        showName = it.showName,
                        posterPath = season.poster_path,
                        showPoster = it.showPoster
                    )
                }
                else -> {}
            }
        }
    }

    override fun onClickMedia(media: Media) {
        val action = FragmentListDirections.actionFragmentListToFragmentMedia(media)
        findNavController().navigateSafe(action)
    }

    override fun onClickCast(cast: Cast) {
        val action = FragmentListDirections.actionFragmentListToCastsFragment(cast)
        findNavController().navigateSafe(action)
    }

    override fun onClickVideo(video: Video) {
        openWebLink(video.watchUrl)
    }

    private fun navigateToSeason(
        tmdbId: Int,
        seasonName: String,
        seasonNumber: Int,
        showName: String?,
        posterPath: String?,
        showPoster: String?
    ) {
        seasonViewModel.setShowSeason(
            tmdbId = tmdbId,
            seasonName = seasonName,
            seasonNumber = seasonNumber,
            showName = showName ?: "Unknown",
            seasonPosterPath = posterPath,
            showPoster = showPoster
        )
        findNavController().navigate(R.id.action_fragmentList_to_episodesListFragment)
    }

    private fun openWebLink(webUrl: String) {
        val launchWebIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
        startActivity(launchWebIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}