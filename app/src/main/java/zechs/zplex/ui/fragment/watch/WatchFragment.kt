package zechs.zplex.ui.fragment.watch

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.adapter.media.AboutDataModel
import zechs.zplex.adapter.media.adapters.CastAdapter
import zechs.zplex.databinding.FragmentWatchBinding
import zechs.zplex.models.dataclass.CastArgs
import zechs.zplex.models.tmdb.StillSize
import zechs.zplex.models.tmdb.entities.Episode
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.viewmodels.EpisodeViewModel
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp
import zechs.zplex.utils.Resource
import java.text.DecimalFormat

class WatchFragment : Fragment(R.layout.fragment_watch) {

    private var _binding: FragmentWatchBinding? = null
    private val binding get() = _binding!!

    private val episodeViewModel by activityViewModels<EpisodeViewModel>()
    private lateinit var watchViewModel: WatchViewModel
    private val castAdapter by lazy { CastAdapter() }

    private var tmdbId: Int? = null
    private var seasonEpisodeText: String? = null

    private val thisTAG = "WatchFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, true
        ).apply {
            duration = 500L
        }

        exitTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, false
        ).apply {
            duration = 500L
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWatchBinding.bind(view)

        watchViewModel = (activity as ZPlexActivity).watchViewModel
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
                is Resource.Error -> {
                }
                is Resource.Loading -> {
                }
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

        context?.let {
            GlideApp.with(it)
                .asBitmap()
                .load(episodeStillUrl)
                .placeholder(R.drawable.no_thumb)
                .into(binding.ivBackdrop)
        }

        val castList = episode.guest_stars?.map {
            AboutDataModel.Cast(
                character = it.character,
                credit_id = it.credit_id,
                person_id = it.id,
                name = it.name,
                profile_path = it.profile_path
            )
        } ?: listOf()

        binding.apply {
            rvCasts.isInvisible = castList.isEmpty()
            textView1.isInvisible = castList.isEmpty()
        }

        castAdapter.differ.submitList(castList)

        val formatter = DecimalFormat("00")
        val seasonEpisode = "S${
            formatter.format(
                episode.season_number
            )
        }E${formatter.format(episode.episode_number)}"
    }

    private fun setupRecyclerView() {
        binding.rvCasts.apply {
            adapter = castAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.HORIZONTAL, false
            )
            itemAnimator = null
        }
        castAdapter.setOnItemClickListener {
            val action = WatchFragmentDirections.actionWatchFragmentToCastsFragment(
                CastArgs(it.credit_id, it.person_id, it.name, it.profile_path)
            )
            findNavController().navigate(action)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding.rvCasts.adapter = null
        _binding = null
    }
}