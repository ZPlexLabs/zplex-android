package zechs.zplex.ui.fragment.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import zechs.zplex.R
import zechs.zplex.adapter.SeasonsAdapter
import zechs.zplex.adapter.list.ListDataModel
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.models.tmdb.entities.Season
import zechs.zplex.ui.BaseFragment
import zechs.zplex.ui.fragment.shared_viewmodels.SeasonViewModel

class FragmentList : BaseFragment() {

    private val thisTAG = "FragmentList"

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val seasonViewModel by activityViewModels<SeasonViewModel>()
    private val listViewModel by activityViewModels<ListViewModel>()

    private var tmdbId: Int? = null
    private var showPoster: String? = null
    private var showName: String? = null

    private val seasonsAdapter by lazy {
        SeasonsAdapter(showName!!) {
            navigateToSeason(
                tmdbId = tmdbId!!,
                seasonName = it.name,
                seasonNumber = it.season_number,
                showName = showName!!,
                posterPath = it.poster_path,
                showPoster = showPoster
            )
        }
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

        listViewModel.listArgs.observe(viewLifecycleOwner) { tmdbList ->
            when (tmdbList) {
                is ListDataModel.Seasons -> handleSeason(tmdbList)
                else -> {}
            }
        }

    }

    private fun handleSeason(it: ListDataModel.Seasons) {
        binding.toolbar.apply {
            title = "Seasons"
            subtitle = it.showName
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        tmdbId = it.tmdbId
        showName = it.showName
        showPoster = it.showPoster

        getSeasonList(it.seasons)

    }

    private fun getSeasonList(seasonList: List<Season>) {
        setupSeasonRecyclerView()
        seasonsAdapter.differ.submitList(seasonList)
    }


    private fun setupSeasonRecyclerView() {
        binding.rvList.apply {
            adapter = seasonsAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
        }
    }

    private fun navigateToSeason(
        tmdbId: Int?,
        seasonName: String,
        seasonNumber: Int,
        showName: String?,
        posterPath: String?,
        showPoster: String?
    ) {
        tmdbId?.let {
            seasonViewModel.setShowSeason(
                tmdbId = it,
                seasonName = seasonName,
                seasonNumber = seasonNumber,
                showName = showName ?: "Unknown",
                seasonPosterPath = posterPath,
                showPoster = showPoster
            )
            findNavController().navigate(R.id.action_fragmentList_to_episodesListFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}