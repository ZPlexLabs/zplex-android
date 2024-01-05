package zechs.zplex.ui.episodes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.ui.cast.CastsFragmentDirections
import zechs.zplex.ui.episodes.adapter.EpisodesDataAdapter
import zechs.zplex.ui.image.BigImageViewModel
import zechs.zplex.ui.shared_viewmodels.EpisodeViewModel
import zechs.zplex.ui.shared_viewmodels.SeasonViewModel
import zechs.zplex.utils.ext.navigateSafe
import zechs.zplex.utils.state.Resource

@AndroidEntryPoint
class EpisodesFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val seasonViewModel by activityViewModels<SeasonViewModel>()
    private val bigImageViewModel by activityViewModels<BigImageViewModel>()
    private val episodeViewModel by activityViewModels<EpisodeViewModel>()

    private val episodesViewModel by lazy {
        ViewModelProvider(this)[EpisodesViewModel::class.java]
    }

    private var hasLoaded: Boolean = false
    private var tmdbId = 0
    private var showName: String? = null
    private var showPoster: String? = null

    private val episodeDataAdapter by lazy {
        EpisodesDataAdapter(
            episodeOnClick = {
                episodeViewModel.setShowEpisode(
                    tmdbId, it.season_number, it.episode_number
                )
                findNavController().navigateSafe(R.id.action_episodesListFragment_to_watchFragment)
            }
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

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupEpisodesViewModel()
    }

    private fun openImageFullSize(posterPath: String?, imageView: ImageView) {
        imageView.transitionName = posterPath
        this.exitTransition = null
        bigImageViewModel.setImagePath(posterPath)

        val action = CastsFragmentDirections.actionCastsFragmentToBigImageFragment()
        val extras = FragmentNavigatorExtras(
            imageView to imageView.transitionName
        )
        findNavController().navigate(action, extras)
        Log.d("navigateToMedia", imageView.transitionName)
    }

    private fun setupEpisodesViewModel() {
        seasonViewModel.showId.observe(viewLifecycleOwner) { showSeason ->
            showName = showSeason.showName
            showPoster = showSeason.showPoster
            tmdbId = showSeason.tmdbId
            if (!hasLoaded) {
                episodesViewModel.getSeason(
                    tmdbId = showSeason.tmdbId,
                    seasonNumber = showSeason.seasonNumber
                )
            }
        }
        episodesViewModel.episodesResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> response.data?.let {
                    TransitionManager.beginDelayedTransition(
                        binding.root,
                        MaterialFadeThrough()
                    )
                    viewLifecycleOwner.lifecycleScope.launch {
                        episodeDataAdapter.submitList(it)
                    }
                    isLoading(false)
                    hasLoaded = true
                }
                is Resource.Error -> {
                    Log.d(TAG, "Error: ${response.message}")
                    showToast(response.message)
                    binding.rvList.isInvisible = true
                }
                is Resource.Loading -> if (!hasLoaded) {
                    isLoading(true)
                }
            }
        }
    }

    private fun isLoading(hide: Boolean) {
        binding.apply {
            loading.isInvisible = !hide
            rvList.isInvisible = hide
        }
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = episodeDataAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(
            context,
            message ?: resources.getString(R.string.something_went_wrong),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.rvList.adapter = null
        _binding = null
    }

    companion object {
        const val TAG = "EpisodesFragment"
    }

}