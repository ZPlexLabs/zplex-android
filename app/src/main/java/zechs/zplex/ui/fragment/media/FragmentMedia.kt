package zechs.zplex.ui.fragment.media

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.adapter.media.MediaClickListener
import zechs.zplex.adapter.media.MediaDataAdapter
import zechs.zplex.adapter.media.MediaDataModel
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.models.Player
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.models.enum.MediaType
import zechs.zplex.models.tmdb.entities.Cast
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.models.tmdb.entities.Season
import zechs.zplex.models.tmdb.entities.Video
import zechs.zplex.ui.BaseFragment
import zechs.zplex.ui.activity.main.MainActivity
import zechs.zplex.ui.activity.player.PlayerActivity
import zechs.zplex.ui.fragment.image.BigImageViewModel
import zechs.zplex.ui.fragment.list.ListViewModel
import zechs.zplex.ui.fragment.shared_viewmodels.SeasonViewModel
import zechs.zplex.ui.movieResponseZplex
import zechs.zplex.utils.ColorManager.Companion.getContrastColor
import zechs.zplex.utils.ColorManager.Companion.isDark
import zechs.zplex.utils.ColorManager.Companion.lightUpColor
import zechs.zplex.utils.Resource
import zechs.zplex.utils.navigateSafe
import java.util.*


class FragmentMedia : BaseFragment(), MediaClickListener {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val mediaDataAdapter by lazy {
        MediaDataAdapter(requireContext(), this)
    }

    // View models
    private lateinit var mediaViewModel: MediaViewModel
    private val seasonViewModel by activityViewModels<SeasonViewModel>()
    private val bigImageViewModel by activityViewModels<BigImageViewModel>()
    private val listViewModel by activityViewModels<ListViewModel>()
    private val args by navArgs<FragmentMediaArgs>()

    // Handling events or saving data
    private var tmdbId: Int? = null
    private var showName: String? = null
    private var showPoster: String? = null
    private var hasLoaded: Boolean = false

    private lateinit var mediaType: MediaType

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

        mediaViewModel = (activity as MainActivity).mediaViewModel
        setupRecyclerView()

        val media = args.media

        mediaType = MediaType.valueOf(
            media.media_type ?: when {
                media.name != null -> "tv"
                media.title != null -> "movie"
                else -> media.media_type
            }!!
        )

        setupMediaViewModel(media.id, mediaType)

        if (mediaType == MediaType.movie) {
            setupZplexMovieObserver()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        tmdbId = media.id
        showName = media.name ?: media.title
        showPoster = media.poster_path
    }

    private fun openImageFullSize(posterPath: String, imageView: ImageView) {
        imageView.transitionName = posterPath
        this.exitTransition = null
        bigImageViewModel.setImagePath(posterPath)

        val action = FragmentMediaDirections.actionFragmentMediaToBigImageFragment()
        val extras = FragmentNavigatorExtras(
            imageView to imageView.transitionName
        )
        findNavController().navigate(action, extras)
        Log.d("navigateToMedia", imageView.transitionName)
    }

    private fun setupZplexMovieObserver() {
        mediaViewModel.movieZplex.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { movie ->
                handleZplexMovieResponse(movie)
            }
        }
    }

    private fun handleZplexMovieResponse(movie: Resource<movieResponseZplex>) {
        when (movie) {
            is Resource.Success -> {
                movie.data?.let {
                    playMovie(Player(it.id, it.name, it.accessToken))
                }
            }
            is Resource.Error -> {
                showToast(movie.message)
            }
            is Resource.Loading -> {}
        }
    }

    private fun setupMediaViewModel(tmdbId: Int, mediaType: MediaType) {
        if (!hasLoaded) {
            mediaViewModel.getMedia(tmdbId, mediaType)
        }
        mediaViewModel.mediaResponse.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is Resource.Success -> response.data?.let {
                        TransitionManager.beginDelayedTransition(
                            binding.root,
                            MaterialFadeThrough()
                        )
                        lifecycleScope.launch {
                            mediaDataAdapter.submitList(it)
                        }
                        isLoading(false)
                        hasLoaded = true
                    }
                    is Resource.Error -> {
                        showToast(response.message)
                        isLoading(false)
                        hasLoaded = true
                    }
                    is Resource.Loading -> {
                        if (!hasLoaded) {
                            mediaViewModel.setDominantColor(
                                Color.parseColor("#EADDFF")
                            )
                            isLoading(true)
                        }
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

    private fun openWebLink(webUrl: String) {
        val launchWebIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
        startActivity(launchWebIntent)
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = mediaDataAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvList.adapter = null
        _binding = null
    }

    private fun shareIntent() {
        val action = FragmentMediaDirections.actionFragmentMediaToShareBottomSheet()
        findNavController().navigateSafe(action)
    }

    private fun shareIntent(tmdbId: Int, showName: String, mediaType: MediaType) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "https://www.themoviedb.org/${mediaType.name}/${tmdbId}"
        )
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, showName)
        startActivity(shareIntent)
    }

    private fun playMovie(player: Player) {
        val intent = Intent(activity, PlayerActivity::class.java)
        intent.putExtra("fileId", player.fileId)
        intent.putExtra("title", showName!!)
        intent.putExtra("accessToken", player.accessToken)
        intent.putExtra("tmdbId", tmdbId!!)
        intent.putExtra("name", showName!!)
        intent.putExtra("posterPath", args.media.poster_path)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity?.startActivity(intent)
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
        if (tmdbId != null && showName != null) {
            listViewModel.setSeasonsList(
                tmdbId!!,
                showName!!,
                showPoster,
                seasons = seasons
            )
            findNavController().navigate(R.id.action_fragmentMedia_to_fragmentList)
        }
    }

    private fun setCastsList(casts: List<Cast>) {
        listViewModel.setCasts(casts = casts)
        findNavController().navigateSafe(R.id.action_fragmentMedia_to_fragmentList)
    }

    private fun navigateToCollection(collectionId: Int) {
        val action = FragmentMediaDirections.actionFragmentMediaToFragmentCollection(collectionId)
        findNavController().navigateSafe(action)
    }

    companion object {
        const val TAG = "FragmentMedia"
    }

    private fun showToast(message: String?) {
        Toast.makeText(
            context,
            message ?: resources.getString(R.string.something_went_wrong),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onClickMedia(media: Media) {
        val action = FragmentMediaDirections.actionFragmentMediaSelf(media)
        findNavController().navigate(action)
    }

    override fun onClickVideo(video: Video) {
        openWebLink(video.watchUrl)
    }

    override fun onClickCast(cast: Cast) {
        val action = FragmentMediaDirections.actionFragmentMediaToCastsFragment(cast)
        findNavController().navigateSafe(action)
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
            val tintColor = ColorStateList.valueOf(color)
            ratingBar.progressTintList = tintColor
            ratingBar.progressBackgroundTintList = tintColor
            ratingBar.secondaryProgressTintList = tintColor
        }
    }

    override fun setButtonView(button: MaterialButton) {
        Log.d(TAG, "${UUID.randomUUID()} setButtonView(), invoked")
        mediaViewModel.dominantColor.observe(viewLifecycleOwner) { c ->
            Log.d(TAG, "${UUID.randomUUID()} setButtonView(), color=$c")
            val color = if (isDark(c)) lightUpColor(c) else c
            val tintColor = ColorStateList.valueOf(color)
            val contrastColor = getContrastColor(color)
            button.backgroundTintList = tintColor
            button.iconTint = ColorStateList.valueOf(contrastColor)
            button.setTextColor(contrastColor)
        }
    }

    override fun lastSeasonClick(lastSeason: MediaDataModel.LatestSeason) {
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

    override fun movieWatchNow(tmdbId: Int) {
        mediaViewModel.zplexGetMovie(tmdbId)
    }

    override fun movieShare(movie: Movie) {
        shareIntent(movie.id, movie.title, MediaType.movie)
    }

    override fun showWatchNow(seasons: List<Season>) {
        setSeasonsList(seasons)
    }

    override fun showShare(show: Show) {
        shareIntent(show.id, show.name, MediaType.tv)
    }

    override fun showWatchlist(view: MaterialButton, show: Show) {
        setupShowDatabaseObserver(show, view)
    }

    override fun movieWatchlist(view: MaterialButton, movie: Movie) {
        setupMovieDatabaseObserver(movie, view)
    }

    private fun setupShowDatabaseObserver(show: Show, view: MaterialButton) {
        var updateSaved = false
        Log.d(TAG, "${UUID.randomUUID()} getShow(), invoked")
        mediaViewModel.getShow(show.id).observe(viewLifecycleOwner) { isSaved ->
            Log.d(TAG, "${UUID.randomUUID()} getShow(), isSaved=$isSaved")
            view.apply {
                icon = ContextCompat.getDrawable(
                    context,
                    if (isSaved) {
                        R.drawable.ic_round_playlist_add_check_24
                    } else R.drawable.ic_add_24
                )
                setOnClickListener {
                    if (isSaved) {
                        mediaViewModel.deleteShow(show)
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
                            mediaViewModel.deleteShow(show)
                        }
                        snackBar.show()
                    }
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
            view.apply {
                icon = ContextCompat.getDrawable(
                    context,
                    if (isSaved) {
                        R.drawable.ic_round_playlist_add_check_24
                    } else R.drawable.ic_add_24
                )
                setOnClickListener {
                    if (isSaved) {
                        mediaViewModel.deleteMovie(movie)
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
                            mediaViewModel.deleteMovie(movie)
                        }
                        snackBar.show()
                    }
                }
            }
            if (isSaved && !updateSaved) {
                mediaViewModel.saveMovie(movie)
                updateSaved = true
            }
        }
    }
}