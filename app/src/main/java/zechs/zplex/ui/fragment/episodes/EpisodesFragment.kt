package zechs.zplex.ui.fragment.episodes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.adapter.EpisodesAdapter
import zechs.zplex.databinding.FragmentEpisodeBinding
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.models.tmdb.season.SeasonResponse
import zechs.zplex.ui.activity.PlayerActivity
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.image.BigImageViewModel
import zechs.zplex.ui.fragment.viewmodels.SeasonViewModel
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp
import zechs.zplex.utils.Resource


class EpisodesFragment : Fragment(R.layout.fragment_episode) {

    private var _binding: FragmentEpisodeBinding? = null
    private val binding get() = _binding!!

    // private val episodeViewModel by activityViewModels<EpisodeViewModel>()
    private val seasonViewModel by activityViewModels<SeasonViewModel>()
    private val bigImageViewModel: BigImageViewModel by activityViewModels()
    private lateinit var episodesViewModel: EpisodesViewModel
    private val episodesAdapter: EpisodesAdapter by lazy { EpisodesAdapter() }

    private val thisTAG = "EpisodesFragment"
    private var tmdbId = 0

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
        _binding = FragmentEpisodeBinding.bind(view)

        episodesViewModel = (activity as ZPlexActivity).episodesViewModel

        binding.rvEpisodes.apply {
            adapter = episodesAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
            itemAnimator = null
        }

        seasonViewModel.showId.observe(viewLifecycleOwner, { showSeason ->
            episodesViewModel.getSeason(
                tvId = showSeason.tmdbId,
                seasonNumber = showSeason.seasonNumber
            )

            val seasonText = "Season ${showSeason.seasonNumber}"
            binding.apply {
                if (showSeason.seasonName == seasonText) {
                    seasonToolbar.title = seasonText
                    seasonToolbar.subtitle = showSeason.showName
                } else {
                    seasonToolbar.title = seasonText
                    seasonToolbar.subtitle = showSeason.seasonName
                }
            }
            tmdbId = showSeason.tmdbId
        })

        episodesViewModel.season.observe(viewLifecycleOwner, { responseMedia ->
            when (responseMedia) {
                is Resource.Success -> {
                    responseMedia.data?.let { seasonResponse ->
                        doOnSuccess(seasonResponse)
                    }
                }

                is Resource.Error -> {
                    responseMedia.message?.let { message ->
                        val errorMsg = if (message.isEmpty()) {
                            resources.getString(R.string.something_went_wrong)
                        } else message
                        Log.e(thisTAG, errorMsg)
                        binding.apply {
                            appBarLayout.isInvisible = true
                            rvEpisodes.isInvisible = true
                            pbEpisodes.isVisible = true
                        }
                        binding.errorView.apply {
                            errorTxt.text = errorMsg
                        }
                    }
                    episodesAdapter.setOnItemClickListener { }
                }

                is Resource.Loading -> {
                    binding.apply {
                        appBarLayout.isInvisible = true
                        rvEpisodes.isInvisible = true
                        pbEpisodes.isVisible = true
                        errorView.root.isVisible = false
                    }
                    episodesAdapter.setOnItemClickListener { }
                }
            }
        })

    }


    private fun doOnSuccess(seasonResponse: SeasonResponse) {

        val posterUrl = if (seasonResponse.poster_path == null) {
            R.drawable.no_poster
        } else {
            "${TMDB_IMAGE_PREFIX}/${PosterSize.w500}${seasonResponse.poster_path}"
        }

        context?.let { c ->
            GlideApp.with(c)
                .load(posterUrl)
                .placeholder(R.drawable.no_poster)
                .into(binding.ivPoster)
        }

        binding.ivPoster.setOnClickListener {
            bigImageViewModel.setImagePath(seasonResponse.poster_path)
            findNavController().navigate(R.id.action_episodesListFragment_to_bigImageFragment)
        }

        val episodesList = seasonResponse.episodes?.toList() ?: listOf()
        episodesAdapter.differ.submitList(episodesList)

        if (episodesList.isEmpty()) {
            val errorMsg = getString(R.string.no_episodes_found)
            Log.e(thisTAG, errorMsg)
            binding.apply {
                appBarLayout.isInvisible = true
                rvEpisodes.isInvisible = true
                pbEpisodes.isInvisible = true
                errorView.root.isVisible = true
            }
            binding.errorView.apply {
                errorTxt.text = errorMsg
                retryBtn.isInvisible = true
            }
        } else {
            binding.apply {
                appBarLayout.isVisible = true
                rvEpisodes.isVisible = true
                pbEpisodes.isInvisible = true
                errorView.root.isVisible = false
            }
        }
        episodesAdapter.setOnItemClickListener { episode ->
            episode.fileId?.let {
                playEpisode(
                    it,
                    if (episode.name.isNullOrEmpty()) "No title" else "Episode ${episode.episode_number} - ${episode.name}"
                )
            }
        }

    }

    private fun playEpisode(fileId: String, title: String) {
        val intent = Intent(activity, PlayerActivity::class.java)
        intent.putExtra("fileId", fileId)
        intent.putExtra("title", title)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity?.startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.rvEpisodes.adapter = null
        _binding = null
    }
}