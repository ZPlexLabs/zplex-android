package zechs.zplex.ui.episodes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.ui.cast.CastsFragmentDirections
import zechs.zplex.ui.episodes.adapter.EpisodesDataAdapter
import zechs.zplex.ui.episodes.adapter.EpisodesDataModel
import zechs.zplex.ui.image.BigImageViewModel
import zechs.zplex.ui.player.MPVActivity
import zechs.zplex.ui.shared_viewmodels.EpisodeViewModel
import zechs.zplex.ui.shared_viewmodels.SeasonViewModel
import zechs.zplex.utils.ext.dpToPx
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
            episodeOnClick = { episode, isLastEpisode ->
                if (episode.fileId != null) {
                    val titleBuilder = StringBuilder()
                    if (showName != null) {
                        titleBuilder.append("$showName - ")
                    }
                    titleBuilder.append(
                        "S%02dE%02d - ".format(
                            episode.season_number,
                            episode.episode_number
                        )
                    )
                    titleBuilder.append(episode.name)

                    episodesViewModel.playEpisode(
                        titleBuilder.toString(),
                        episode.season_number,
                        episode.episode_number,
                        isLastEpisode,
                        episode.fileId,
                    )
                } else {
                    if (!episodesViewModel.hasLoggedIn) {
                        val snackBar = Snackbar.make(
                            binding.root,
                            getString(R.string.login_to_google_drive),
                            Snackbar.LENGTH_SHORT
                        )
                        snackBar.setAction(getString(R.string.go_to_settings)) {
                            findNavController().navigateSafe(R.id.action_episodesListFragment_to_settingsFragment)
                        }
                        snackBar.show()
                    } else {
                        episodeViewModel.setShowEpisode(
                            tmdbId,
                            episode.season_number,
                            episode.episode_number
                        )
                        findNavController().navigateSafe(R.id.action_episodesListFragment_to_watchFragment)
                    }
                }
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
        mpvObserver()
        setupLastWatchedEpisode()
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
                episodesViewModel.getSeasonWithWatched(
                    tmdbId = showSeason.tmdbId,
                    seasonNumber = showSeason.seasonNumber
                )
            }
        }
        episodesViewModel.episodesWithWatched.observe(viewLifecycleOwner) { response ->
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

    private fun setupLastWatchedEpisode() {
        val viewId = ViewCompat.generateViewId()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                episodesViewModel.lastEpisode.collect { episode ->
                    if (episode != null) {
                        showResumeEpisode(viewId, episode)
                    }
                }

            }
        }
    }

    private fun showResumeEpisode(
        viewId: Int,
        episode: EpisodesDataModel.Episode
    ) {
        Log.d(TAG, "Found last episode: $episode")

        val exist = binding.coordinatorLayout.findViewById<ExtendedFloatingActionButton>(viewId)
        val extendedFab: ExtendedFloatingActionButton

        Log.d(TAG, "Extended fab: $exist")

        if (exist == null) {
            extendedFab = ExtendedFloatingActionButton(requireContext())
            extendedFab.id = viewId
            extendedFab.text = getString(R.string.continue_watching)
            extendedFab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_resume_24)

            val params = CoordinatorLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.gravity = Gravity.BOTTOM or Gravity.END
            params.bottomMargin = resources.dpToPx(16)
            params.rightMargin = resources.dpToPx(16)

            binding.coordinatorLayout.addView(extendedFab, params)
            extendedFab.doOnLayout { showSlideUp(extendedFab) }
        } else {
            extendedFab = exist
        }

        extendedFab.setOnClickListener {
            val titleBuilder = StringBuilder()
            if (showName != null) {
                titleBuilder.append("$showName - ")
            }
            titleBuilder.append(
                "S%02dE%02d - ".format(
                    episode.season_number,
                    episode.episode_number
                )
            )
            titleBuilder.append(episode.name)
            episodesViewModel.playEpisode(
                titleBuilder.toString(),
                episode.season_number,
                episode.episode_number,
                isLastEpisode = false,
                episode.fileId!!
            )
        }
    }


    private fun showSlideUp(view: View) {
        val initialTranslationY = view.height + view.marginBottom.toFloat()
        view.translationY = initialTranslationY
        view.animate()
            .translationY(0f)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(250L)
            .start()
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

    private fun mpvObserver() {
        episodesViewModel.mpvFile.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { res ->
                when (res) {
                    is Resource.Success -> {
                        launchMpv(res.data!!)
                    }

                    is Resource.Error -> {
                        Snackbar.make(
                            binding.root,
                            res.message ?: resources.getString(R.string.something_went_wrong),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun launchMpv(fileToken: EpisodesViewModel.FileToken) {
        Intent(
            requireContext(), MPVActivity::class.java
        ).apply {
            putExtra("fileId", fileToken.fileId)
            putExtra("title", fileToken.fileName)
            putExtra("accessToken", fileToken.accessToken)
            putExtra("isTV", true)
            putExtra("tmdbId", tmdbId)
            putExtra("name", showName ?: "")
            putExtra("posterPath", showPoster ?: "")

            putExtra("seasonNumber", fileToken.seasonNumber)
            putExtra("episodeNumber", fileToken.episodeNumber)
            putExtra("isLastEpisode", fileToken.isLastEpisode)

            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.also { startActivity(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.rvList.adapter = null
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        episodesViewModel.updateStatus()
    }

    companion object {
        const val TAG = "EpisodesFragment"
    }

}