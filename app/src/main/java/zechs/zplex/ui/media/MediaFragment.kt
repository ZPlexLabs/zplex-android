package zechs.zplex.ui.media

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateInterpolator
import android.widget.ImageView
import android.widget.RatingBar
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.doOnAttach
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import zechs.zplex.R
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.entities.Show
import zechs.zplex.data.model.tmdb.entities.Cast
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.data.model.tmdb.entities.Season
import zechs.zplex.data.model.tmdb.entities.Video
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.ui.image.BigImageViewModel
import zechs.zplex.ui.list.ListViewModel
import zechs.zplex.ui.list.adapter.ListDataModel
import zechs.zplex.ui.media.adapter.MediaClickListener
import zechs.zplex.ui.media.adapter.MediaDataAdapter
import zechs.zplex.ui.media.adapter.MediaDataModel
import zechs.zplex.ui.player.MPVActivity
import zechs.zplex.ui.shared_viewmodels.SeasonViewModel
import zechs.zplex.utils.ext.navigateSafe
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.util.ColorManager.Companion.getContrastColor
import zechs.zplex.utils.util.ColorManager.Companion.isDark
import zechs.zplex.utils.util.ColorManager.Companion.lightUpColor
import java.util.UUID


@AndroidEntryPoint
class MediaFragment : Fragment() {

    companion object {
        const val TAG = "MediaFragment"
    }

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    // View models
    private val seasonViewModel by activityViewModels<SeasonViewModel>()
    private val bigImageViewModel by activityViewModels<BigImageViewModel>()
    private val listDataViewModel by activityViewModels<ListViewModel>()
    private val mediaViewModel by lazy {
        ViewModelProvider(this)[MediaViewModel::class.java]
    }

    // Nav-args
    private val args by navArgs<MediaFragmentArgs>()

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

        val media = args.media
        mediaViewModel.mediaType = when {
            media.name != null -> MediaType.tv
            media.title != null -> MediaType.movie
            else -> media.media_type!!
        }

        setupRecyclerView()
        setupMediaViewModel(media.id, mediaViewModel.mediaType)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        mpvObserver()
    }

    private fun setupMediaViewModel(tmdbId: Int, mediaType: MediaType) {
        if (!mediaViewModel.hasLoaded) {
            mediaViewModel.getMedia(tmdbId, mediaType)
        }
        mediaViewModel.mediaResponse.observe(viewLifecycleOwner) { response ->
            handleMediaResponse(response)
        }
    }

    private fun handleMediaResponse(
        response: Resource<List<MediaDataModel>>
    ) {
        when (response) {
            is Resource.Success -> response.data?.let {
                requireView().doOnAttach {
                    TransitionManager.beginDelayedTransition(
                        binding.root,
                        MaterialFadeThrough()
                    )

                    mediaDataAdapter.submitList(response.data)
                    startPostponedEnterTransition()
                }

                isLoading(false)
                mediaViewModel.hasLoaded = true
            }

            is Resource.Error -> {
                requireView().doOnAttach { startPostponedEnterTransition() }
                showSnackBar(
                    message = response.message,
                    action = SnackBarAction(R.string.retry) {
                        mediaViewModel.getMedia(
                            mediaViewModel.tmdbId,
                            mediaViewModel.mediaType
                        )
                    }

                )
                isLoading(false)
                mediaViewModel.hasLoaded = true
            }

            is Resource.Loading -> {
                if (!mediaViewModel.hasLoaded) {
                    mediaViewModel.setDominantColor("#EADDFF".toColorInt())
                    isLoading(true)
                    postponeEnterTransition()
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

    private val mediaDataAdapter by lazy {
        MediaDataAdapter(mediaClickListener = object : MediaClickListener {

            override fun onClickViewAll(
                listDataModel: ListDataModel
            ) {
                when (listDataModel) {
                    is ListDataModel.Casts -> setCastsList(listDataModel.casts)
                    is ListDataModel.Media -> setMediaList(
                        listDataModel.heading,
                        listDataModel.media
                    )

                    is ListDataModel.Videos -> setVideoList(listDataModel.videos)
                    else -> {}
                }
            }

            override fun onClickMedia(media: Media) {
                val action = MediaFragmentDirections
                    .actionFragmentMediaSelf(media)
                findNavController().navigate(action)
            }

            override fun onClickVideo(video: Video) {
                openWebLink(video.watchUrl)
            }

            override fun onClickCast(cast: Cast) {
                cast.id?.let { castId ->
                    val action = MediaFragmentDirections
                        .actionFragmentMediaToCastsFragment(castId)
                    findNavController().navigateSafe(action)
                }
            }

            override fun setImageResource(image: Drawable) {
                Log.d(TAG, "${UUID.randomUUID()} setImageResource(), invoked")
                mediaViewModel.calcDominantColor(image) { color ->
                    Log.d(TAG, "${UUID.randomUUID()} setImageResource(), color=$color")
                    mediaViewModel.setDominantColor(color)
                }
            }

            override fun setRatingBarView(ratingBar: RatingBar) {
                Log.d(TAG, "${UUID.randomUUID()} setRatingBarView(), invoked")
                mediaViewModel.dominantColor.observe(viewLifecycleOwner) { c ->
                    Log.d(TAG, "${UUID.randomUUID()} setRatingBarView(), color=$c")
                    val color = if (isDark(c)) lightUpColor(c) else c
                    val sourceColor = ContextCompat.getColor(
                        requireContext(),
                        R.color.colorAccent
                    )
                    with(
                        ValueAnimator.ofObject(
                            ArgbEvaluator(),
                            sourceColor, color
                        )
                    ) {
                        interpolator = AnticipateInterpolator()
                        duration = 500L // milliseconds
                        addUpdateListener { animator ->
                            val tintColor = ColorStateList.valueOf(animator.animatedValue as Int)
                            ratingBar.progressTintList = tintColor
                            ratingBar.progressBackgroundTintList = tintColor
                            ratingBar.secondaryProgressTintList = tintColor
                        }
                        start()
                    }

                }
            }

            override fun setButtonView(button: MaterialButton) {
                Log.d(TAG, "${UUID.randomUUID()} setButtonView(), invoked")
                mediaViewModel.dominantColor.observe(viewLifecycleOwner) { c ->
                    Log.d(TAG, "${UUID.randomUUID()} setButtonView(), color=$c")
                    val color = if (isDark(c)) lightUpColor(c) else c
                    val contrastColor = getContrastColor(color)
                    val sourceColor = ContextCompat.getColor(
                        requireContext(),
                        R.color.colorAccent
                    )
                    with(
                        ValueAnimator.ofObject(
                            ArgbEvaluator(),
                            sourceColor, color
                        )
                    ) {
                        interpolator = AnticipateInterpolator()
                        duration = 500L // milliseconds
                        addUpdateListener { animator ->
                            val tintColor = ColorStateList.valueOf(animator.animatedValue as Int)
                            button.backgroundTintList = tintColor
                        }
                        start()
                    }
                    button.iconTint = ColorStateList.valueOf(contrastColor)
                    button.setTextColor(contrastColor)
                }
            }

            override fun lastSeasonClick(
                lastSeason: MediaDataModel.LatestSeason
            ) {
                navigateToSeason(
                    tmdbId = lastSeason.showTmdbId,
                    seasonName = lastSeason.seasonName,
                    seasonNumber = lastSeason.seasonNumber,
                    showName = lastSeason.showName,
                    seasonPosterPath = lastSeason.seasonPosterPath,
                    showPoster = lastSeason.showPoster
                )
            }

            override fun collectionClick(collectionId: Int) {
                navigateToCollection(collectionId)
            }

            override fun setMovieWatchNowButton(view: MaterialButton) {
                mediaViewModel.movieWatchedState(args.media.id)
                    .observe(viewLifecycleOwner) { watchedMovie ->
                        val continueWatching = watchedMovie != null && !watchedMovie.hasFinished()
                        if (continueWatching) {
                            view.text = getString(R.string.continue_watching)
                            view.icon =
                                ContextCompat.getDrawable(requireContext(), R.drawable.ic_resume_24)
                        } else {
                            view.text = getString(R.string.watch_now)
                            view.icon = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_play_circle_24
                            )
                        }
                    }
            }

            override fun movieWatchNow(movie: Movie, year: Int?) {
                if (mediaViewModel.hasLoggedIn) {
                    mediaViewModel.playMovie(movie, year)
                } else {
                    val snackBar = Snackbar.make(
                        binding.root,
                        getString(R.string.login_to_google_drive),
                        Snackbar.LENGTH_SHORT
                    )
                    snackBar.setAction(getString(R.string.go_to_settings)) {
                        findNavController().navigateSafe(R.id.action_fragmentMedia_to_settingsFragment)
                    }
                    snackBar.show()
                }
            }

            override fun movieShare(tmdbId: Int, title: String, imdbId: String?) {
                shareIntent(tmdbId, imdbId, title, MediaType.movie)
            }

            override fun showWatchNow(seasons: List<Season>) {
                setSeasonsList(seasons)
            }

            override fun showShare(tmdbId: Int, title: String, imdbId: String?) {
                shareIntent(tmdbId, imdbId, title, MediaType.tv)
            }

            override fun showWatchlist(
                view: MaterialButton,
                show: Show
            ) {
                setupShowDatabaseObserver(show, view)
            }

            override fun movieWatchlist(
                view: MaterialButton,
                movie: Movie
            ) {
                setupMovieDatabaseObserver(movie, view)
            }

            override fun openImageInBig(
                imagePath: String?,
                imageView: ImageView
            ) {
                imagePath?.let { openImageFullSize(imagePath, imageView) }
            }
        })
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = mediaDataAdapter
            layoutManager = LinearLayoutManager(
                activity, RecyclerView.VERTICAL, false
            )
            setItemViewCacheSize(15)
        }
    }

    private fun openWebLink(webUrl: String) {
        val launchWebIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
        startActivity(launchWebIntent)
    }

    private fun openImageFullSize(posterPath: String, imageView: ImageView) {
        imageView.transitionName = posterPath
        this.exitTransition = null
        bigImageViewModel.setImagePath(posterPath)

        val action = MediaFragmentDirections.actionFragmentMediaToBigImageFragment()
        val extras = FragmentNavigatorExtras(
            imageView to imageView.transitionName
        )
        findNavController().navigate(action, extras)
        Log.d("navigateToMedia", imageView.transitionName)
    }

    private fun shareIntent(
        tmdbId: Int,
        imdbId: String?,
        title: String,
        mediaType: MediaType
    ) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        val shareUrl = if (imdbId != null) {
            "https://www.imdb.com/title/${imdbId}"
        } else "https://www.themoviedb.org/${mediaType.name}/${tmdbId}"
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrl)
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, title)
        startActivity(shareIntent)
    }

    private fun navigateToSeason(
        tmdbId: Int,
        seasonName: String,
        seasonNumber: Int,
        showName: String?,
        seasonPosterPath: String?,
        showPoster: String?
    ) {
        seasonViewModel.setShowSeason(
            tmdbId = tmdbId,
            seasonName = seasonName,
            seasonNumber = seasonNumber,
            showName = showName ?: "Unknown",
            seasonPosterPath = seasonPosterPath,
            showPoster = showPoster
        )
        findNavController().navigateSafe(R.id.action_fragmentMedia_to_episodeListFragment)
    }

    private fun setSeasonsList(seasons: List<Season>) {
        if (mediaViewModel.showName != null) {
            listDataViewModel.setSeasonsList(
                mediaViewModel.tmdbId,
                mediaViewModel.showName!!,
                mediaViewModel.showPoster,
                seasons = seasons
            )
            findNavController().navigate(R.id.action_fragmentMedia_to_fragmentList)
        } else {
            showSnackBar()
        }
    }

    @Keep
    private data class SnackBarAction(
        @StringRes val resId: Int,
        val listener: View.OnClickListener
    )

    private fun showSnackBar(
        message: String? = null,
        duration: Int = Snackbar.LENGTH_SHORT,
        action: SnackBarAction? = null
    ) {
        Snackbar.make(
            binding.root,
            message ?: resources.getString(R.string.something_went_wrong),
            Snackbar.LENGTH_SHORT
        ).also {
            action?.let { a ->
                it.setAction(a.resId, a.listener)
            }
            it.duration = duration
        }.show()
    }

    private fun setCastsList(casts: List<Cast>) {
        listDataViewModel.setCasts(casts = casts)
        findNavController().navigateSafe(R.id.action_fragmentMedia_to_fragmentList)
    }

    private fun setMediaList(heading: String, media: List<Media>) {
        listDataViewModel.setMedia(heading, media)
        findNavController().navigateSafe(R.id.action_fragmentMedia_to_fragmentList)
    }

    private fun setVideoList(videos: List<Video>) {
        listDataViewModel.setVideo(videos)
        findNavController().navigateSafe(R.id.action_fragmentMedia_to_fragmentList)
    }

    private fun navigateToCollection(collectionId: Int) {
        val action = MediaFragmentDirections
            .actionFragmentMediaToFragmentCollection(
                collectionId
            )
        findNavController().navigateSafe(action)
    }


    private fun setupShowDatabaseObserver(show: Show, view: MaterialButton) {
        var updateSaved = false
        Log.d(TAG, "${UUID.randomUUID()} getShow(), invoked")

        mediaViewModel.getShow(show.id).observe(viewLifecycleOwner) { isSaved ->
            Log.d(TAG, "${UUID.randomUUID()} getShow(), isSaved=$isSaved")

            view.icon = ContextCompat.getDrawable(
                view.context,
                if (isSaved) {
                    R.drawable.ic_saved_24
                } else R.drawable.ic_add_24
            )

            view.setOnClickListener {
                if (isSaved) {
                    mediaViewModel.deleteShow(show.id)
                    val snackBar = Snackbar.make(
                        binding.rvList, "${show.name} removed from your library",
                        Snackbar.LENGTH_SHORT
                    )
                    snackBar.setAction(
                        R.string.undo
                    ) {
                        mediaViewModel.saveShow(show)
                    }
                    snackBar.show()
                } else {
                    mediaViewModel.saveShow(show)
                    val snackBar = Snackbar.make(
                        binding.rvList, "${show.name} added to your library",
                        Snackbar.LENGTH_SHORT
                    )
                    snackBar.setAction(
                        R.string.undo
                    ) {
                        mediaViewModel.deleteShow(show.id)
                    }
                    snackBar.show()
                }
            }

            if (isSaved && !updateSaved) {
                mediaViewModel.saveShow(show)
                updateSaved = true
            }
        }
    }

    private fun setupMovieDatabaseObserver(movie: Movie, view: MaterialButton) {
        var updateSaved = false
        Log.d(TAG, "${UUID.randomUUID()} getMovie(), invoked")

        mediaViewModel.getMovie(movie.id).observe(viewLifecycleOwner) { isSaved ->
            Log.d(TAG, "${UUID.randomUUID()} getMovie(), isSaved=$isSaved")

            view.icon = ContextCompat.getDrawable(
                view.context,
                if (isSaved) {
                    R.drawable.ic_saved_24
                } else R.drawable.ic_add_24
            )

            view.setOnClickListener {
                if (isSaved) {
                    mediaViewModel.deleteMovie(movie.id)
                    val snackBar = Snackbar.make(
                        binding.rvList, "${movie.title} removed from your library",
                        Snackbar.LENGTH_SHORT
                    )
                    snackBar.setAction(
                        R.string.undo
                    ) {
                        mediaViewModel.saveMovie(movie)
                    }
                    snackBar.show()
                } else {
                    mediaViewModel.saveMovie(movie)
                    val snackBar = Snackbar.make(
                        binding.rvList, "${movie.title} added to your library",
                        Snackbar.LENGTH_SHORT
                    )
                    snackBar.setAction(
                        R.string.undo
                    ) {
                        mediaViewModel.deleteMovie(movie.id)
                    }
                    snackBar.show()
                }
            }
            if (isSaved && !updateSaved) {
                mediaViewModel.saveMovie(movie)
                updateSaved = true
            }
        }
    }

    private fun mpvObserver() {
        mediaViewModel.movieFile.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { res ->
                when (res) {
                    is Resource.Success -> {
                        launchMpv(res.data!!)
                    }

                    is Resource.Error -> {
                        showSnackBar(res.message!!)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun launchMpv(movie: zechs.zplex.ui.player.Movie) {
        Intent(
            requireContext(), MPVActivity::class.java
        ).apply {
            putExtra("playlist", Gson().toJson(listOf(movie)))
            putExtra("startIndex", 0)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.also { startActivity(it) }
    }

    override fun onStart() {
        super.onStart()
        mediaViewModel.updateStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvList.adapter = null
        _binding = null
    }

}