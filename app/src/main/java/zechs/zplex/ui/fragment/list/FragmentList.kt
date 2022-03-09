package zechs.zplex.ui.fragment.list

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Fade
import androidx.transition.TransitionSet
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.adapter.SeasonsAdapter
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.models.dataclass.ListArgs
import zechs.zplex.models.tmdb.entities.Cast
import zechs.zplex.models.tmdb.entities.Season
import zechs.zplex.ui.fragment.viewmodels.SeasonViewModel

class FragmentList : Fragment(R.layout.fragment_list) {

    private val thisTAG = "FragmentList"

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val seasonViewModel by activityViewModels<SeasonViewModel>()
    private val listViewModel by activityViewModels<ListViewModel>()

    private var tmdbId: Int? = null
    private var showName: String? = null

    private val seasonsAdapter by lazy {
        SeasonsAdapter(showName!!) {
            navigateToSeason(
                tmdbId = tmdbId!!,
                seasonName = it.name,
                seasonNumber = it.season_number,
                showName = showName!!,
                posterPath = it.poster_path
            )
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = TransitionSet().apply {
            addTransition(
                MaterialSharedAxis(
                    MaterialSharedAxis.Y, true
                ).apply {
                    interpolator = LinearInterpolator()
                    duration = 500
                })

            addTransition(Fade().apply {
                interpolator = LinearInterpolator()
            })
        }

        exitTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, true
        ).apply {
            interpolator = LinearInterpolator()
            duration = 500
        }

        returnTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, false
        ).apply {
            interpolator = LinearInterpolator()
            duration = 220
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentListBinding.bind(view)

        listViewModel.listArgs.observe(viewLifecycleOwner) {
            binding.toolbar.apply {
                setNavigationOnClickListener { findNavController().navigateUp() }
                subtitle = it.showName
            }

            tmdbId = it.tmdbId
            showName = it.showName

            if (it.castList == null && it.seasonList != null) {
                getSeasonList(it.seasonList)
            } else if (it.castList != null && it.seasonList == null) {
                getCastList(it.castList)
            } else {
                throwError(it)
            }
        }

    }


    private fun getSeasonList(seasonList: List<Season>) {
        binding.toolbar.title = "Seasons"
        setupSeasonRecyclerView()
        seasonsAdapter.differ.submitList(seasonList)
    }

    private fun getCastList(castList: List<Cast>) {
        TODO("Not yet implemented")
    }

    private fun throwError(listArgs: ListArgs) {
        Log.d(thisTAG, "[ERROR] ListArgs=$listArgs")
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
        posterPath: String?
    ) {
        tmdbId?.let {
            seasonViewModel.setShowSeason(
                tmdbId = it,
                seasonName = seasonName,
                seasonNumber = seasonNumber,
                showName = showName ?: "Unknown",
                posterPath = posterPath
            )
            findNavController().navigate(R.id.action_fragmentList_to_episodesListFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}