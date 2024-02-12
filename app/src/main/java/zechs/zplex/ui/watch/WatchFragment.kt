package zechs.zplex.ui.watch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import zechs.zplex.R
import zechs.zplex.data.model.StillSize
import zechs.zplex.data.model.tmdb.entities.Cast
import zechs.zplex.data.model.tmdb.entities.Episode
import zechs.zplex.databinding.FragmentWatchBinding
import zechs.zplex.ui.shared_adapters.casts.CastAdapter
import zechs.zplex.ui.shared_viewmodels.EpisodeViewModel
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.ext.navigateSafe
import zechs.zplex.utils.state.Resource

@AndroidEntryPoint
class WatchFragment : Fragment() {

    private var _binding: FragmentWatchBinding? = null
    private val binding get() = _binding!!

    private val episodeViewModel by activityViewModels<EpisodeViewModel>()
    private val watchViewModel by lazy {
        ViewModelProvider(this)[WatchViewModel::class.java]
    }
    private val castAdapter by lazy {
        CastAdapter(castOnClick = { cast ->
            cast.id?.let { castId ->
                WatchFragmentDirections
                    .actionWatchFragmentToCastsFragment(castId)
                    .also {
                        findNavController().navigateSafe(it)
                    }
            }
        })
    }

    private var tmdbId: Int? = null
    private var seasonEpisodeText: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWatchBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()

        episodeViewModel.showEpisode.observe(viewLifecycleOwner) {
            tmdbId = it.tmdbId
            watchViewModel.getEpisode(
                it.tmdbId, it.seasonNumber, it.episodeNumber
            )
        }

        watchViewModel.episode.observe(viewLifecycleOwner) { responseEpisode ->
            when (responseEpisode) {
                is Resource.Success -> {
                    responseEpisode.data?.let { doOnSuccess(it) }
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }

    }

    private fun doOnSuccess(episode: Episode) {

        val episodeStillUrl = if (episode.still_path == null) {
            R.drawable.no_thumb
        } else {
            "${TMDB_IMAGE_PREFIX}/${StillSize.original}${episode.still_path}"
        }

        seasonEpisodeText = "Season ${episode.season_number}, Episode ${episode.episode_number}"

        binding.apply {
            tvSeasonEpisode.text = seasonEpisodeText
            tvTitle.text = episode.name
            tvOverview.text = if (episode.overview.isNullOrEmpty()) {
                "No description"
            } else episode.overview
            tvOverview.setOnClickListener {
                TransitionManager.beginDelayedTransition(binding.root)
                tvOverview.maxLines = if (tvOverview.lineCount > 4) 4 else 1000
            }
        }

        binding.ivBackdrop.load(episodeStillUrl) {
            placeholder(R.drawable.no_thumb)
        }

        val castList = episode.guest_stars?.map {
            Cast(
                character = it.character,
                credit_id = it.credit_id,
                id = it.id,
                name = it.name,
                profile_path = it.profile_path
            )
        } ?: listOf()

        binding.apply {
            rvCasts.isInvisible = castList.isEmpty()
            textView1.isInvisible = castList.isEmpty()
        }

        castAdapter.submitList(castList)
    }

    private fun setupRecyclerView() {
        binding.rvCasts.apply {
            adapter = castAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.HORIZONTAL, false
            )
            itemAnimator = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCasts.adapter = null
        _binding = null
    }
}