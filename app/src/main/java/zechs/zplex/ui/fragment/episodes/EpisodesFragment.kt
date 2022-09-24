package zechs.zplex.ui.fragment.episodes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFadeThrough
import zechs.zplex.R
import zechs.zplex.adapter.episodes.EpisodesDataAdapter
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.models.zplex.Episode
import zechs.zplex.ui.BaseFragment
import zechs.zplex.ui.fragment.cast.CastsFragmentDirections
import zechs.zplex.ui.fragment.collection.FragmentCollectionDirections
import zechs.zplex.ui.fragment.image.BigImageViewModel
import zechs.zplex.ui.fragment.shared_viewmodels.SeasonViewModel
import zechs.zplex.utils.Resource
import zechs.zplex.utils.navigateSafe

class EpisodesFragment : BaseFragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val seasonViewModel by activityViewModels<SeasonViewModel>()
    private val bigImageViewModel by activityViewModels<BigImageViewModel>()
    private val episodesViewModel by activityViewModels<EpisodesViewModel>()

    private var hasLoaded: Boolean = false
    private var tmdbId = 0
    private var showName: String? = null
    private var showPoster: String? = null

    private val episodeDataAdapter by lazy {
        EpisodesDataAdapter(
            context = requireContext(),
            setOnEpisodeClick = { e, t, b ->
                playEpisode(e, t, b)
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

    private fun navigateMedia(media: Media) {
        val action = FragmentCollectionDirections.actionFragmentCollectionToFragmentMedia(
            media.copy(media_type = media.media_type ?: "movie")
        )
        Log.d(TAG, "navigateMedia, invoked. ($media)")
        findNavController().navigateSafe(action)
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
        episodesViewModel.episodesResponse.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is Resource.Success -> response.data?.let {
                        TransitionManager.beginDelayedTransition(
                            binding.root,
                            MaterialFadeThrough()
                        )
                        episodeDataAdapter.submitList(it)
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

    private fun playEpisode(
        episode: Episode, accessToken: String, isLastEpisode: Boolean
    ) {
//        episode.file_id?.let { id ->
//            val title = if (episode.name.isEmpty()) {
//                "No title"
//            } else "Episode ${episode.episode_number} - ${episode.name}"
//            val intent = Intent(activity, PlayerActivity::class.java)
//            intent.putExtra("fileId", id)
//            intent.putExtra("title", title)
//            intent.putExtra("accessToken", accessToken)
//            intent.putExtra("tmdbId", tmdbId)
//            intent.putExtra("name", showName!!)
//            intent.putExtra("posterPath", showPoster)
//            intent.putExtra("seasonNumber", episode.season_number)
//            intent.putExtra("episodeNumber", episode.episode_number)
//            intent.putExtra("isTV", true)
//            intent.putExtra("isLastEpisode", isLastEpisode)
//
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            activity?.startActivity(intent)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvList.adapter = null
        _binding = null
    }

    companion object {
        const val TAG = "EpisodesFragment"
    }

}