package zechs.zplex.ui.fragment.media

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.adapter.media.AboutDataModel
import zechs.zplex.adapter.media.MediaDataAdapter
import zechs.zplex.adapter.media.MediaDataModel
import zechs.zplex.databinding.FragmentMediaBinding
import zechs.zplex.models.dataclass.MediaArgs
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.models.drive.File
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.models.tmdb.media.MediaResponse
import zechs.zplex.ui.activity.PlayerActivity
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.image.BigImageViewModel
import zechs.zplex.ui.fragment.viewmodels.CastDetailsViewModel
import zechs.zplex.ui.fragment.viewmodels.SeasonViewModel
import zechs.zplex.ui.fragment.viewmodels.ShowViewModel
import zechs.zplex.utils.Constants.ZPLEX_MOVIES_ID
import zechs.zplex.utils.Constants.ZPLEX_SHOWS_ID
import zechs.zplex.utils.Resource

class FragmentMedia : Fragment(R.layout.fragment_media) {

    private var _binding: FragmentMediaBinding? = null
    private val binding get() = _binding!!

    private val seasonViewModel by activityViewModels<SeasonViewModel>()
    private val showsViewModel by activityViewModels<ShowViewModel>()
    private val castDetailsViewModel by activityViewModels<CastDetailsViewModel>()
    private val bigImageViewModel: BigImageViewModel by activityViewModels()
    private lateinit var mediaViewModel: MediaViewModel

    private val mediaDataAdapter by lazy { MediaDataAdapter() }

    private val thisTAG = "FragmentMedia"
    private var driveId: String? = null
    private var mediaArgs = MediaArgs(0, "none", null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, true
        ).apply {
            duration = 500L
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMediaBinding.bind(view)

        mediaViewModel = (activity as ZPlexActivity).mediaViewModel

        setupRecyclerView()
        setupShowsViewModel()
        setupMediaViewModel()

    }

    private fun setupSearchObserverForMovie() {
        mediaViewModel.searchList.observe(viewLifecycleOwner, { responseEpisode ->
            when (responseEpisode) {
                is Resource.Success -> {
                    responseEpisode.data?.let { driveResponse ->
                        binding.toolbarMovie.apply {
                            btnWatchNow.isVisible = true
                            pbWatch.isInvisible = true
                        }
                        if (driveResponse.files.isNotEmpty()) {
                            val file = driveResponse.files[0]
                            binding.toolbarMovie.btnWatchNow.apply {
                                text = resources.getString(R.string.watch_now)
                                icon = ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_round_play_circle_outline_24
                                )
                                setOnClickListener { playMovie(file) }
                            }
                        } else {
                            binding.toolbarMovie.btnWatchNow.apply {
                                text = resources.getString(R.string.request_this_movie)
                                icon = ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_round_queue_play_next_24
                                )
                                setOnClickListener {
                                    Toast.makeText(
                                        context,
                                        "Coming soon!", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    binding.toolbarMovie.apply {
                        root.isVisible = true
                        btnWatchNow.isInvisible = true
                        pbWatch.isVisible = false
                        btnWatchNow.setOnClickListener(null)
                        btnShare.setOnClickListener(null)
                    }
                }
                is Resource.Loading -> {
                    binding.toolbarMovie.apply {
                        root.isVisible = true
                        btnWatchNow.isInvisible = true
                        pbWatch.isVisible = true
                        btnWatchNow.setOnClickListener(null)
                    }
                    binding.toolbarTv.root.isVisible = false
                }
            }
        })
    }


    private fun setupSearchObserverForTV() {
        mediaViewModel.searchList.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { driveResponse ->
                        driveId = if (driveResponse.files.isNotEmpty()) {
                            driveResponse.files[0].id
                        } else null
                    }
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        val errorMsg = if (message.isEmpty()) {
                            resources.getString(R.string.something_went_wrong)
                        } else message
                        Log.e(thisTAG, errorMsg)
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    binding.toolbarMovie.root.isVisible = false
                    binding.toolbarTv.root.isVisible = true
                }
            }
        })
    }

    private fun playMovie(file: File) {
        val intent = Intent(activity, PlayerActivity::class.java)
        intent.putExtra("fileId", file.id)
        intent.putExtra("title", mediaArgs.media?.title)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        activity?.startActivity(intent)
    }

    private fun setupShowsViewModel() {
        showsViewModel.mediaArgs.observe(viewLifecycleOwner, { media ->
            val mediaType = if (media.mediaType == "none") "tv" else media.mediaType
            mediaArgs = MediaArgs(media.tmdbId, mediaType, media.media)

            if (mediaType == "movie") {
                setupMovieDatabaseObserver(mediaArgs)
                setupSearchObserverForMovie()
                mediaViewModel.doSearchFor(searchQuery(media.tmdbId, mediaType))
                binding.apply {
                    toolbarTv.root.isVisible = false
                    toolbarMovie.root.isVisible = true
                }
            } else {
                setupShowDatabaseObserver(mediaArgs)
                setupSearchObserverForTV()
                mediaViewModel.doSearchFor(searchQuery(media.tmdbId, mediaType))
                binding.apply {
                    toolbarMovie.root.isVisible = false
                    toolbarTv.root.isVisible = true
                }
            }

            if (mediaType == "tv" || mediaType == "movie") {
                mediaViewModel.getMedia(media.tmdbId, mediaType)
                binding.errorView.retryBtn.setOnClickListener {
                    mediaViewModel.getMedia(media.tmdbId, mediaType)
                }
            } else {
                val errorMsg = "Unknown media type..."
                Log.e(thisTAG, errorMsg + media)
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                binding.apply {
                    pbTv.isGone = true
                    successView.isVisible = false
                    errorView.root.isVisible = true
                }
                binding.errorView.apply {
                    errorTxt.text = errorMsg
                }
            }
        })
    }

    private fun setupShowDatabaseObserver(media: MediaArgs) {

        mediaViewModel.getShow(media.tmdbId).observe(viewLifecycleOwner, { isSaved ->
            Log.d("getShow", "isSaved=$isSaved")

            binding.toolbarTv.btnWatchlist.apply {
                icon = ContextCompat.getDrawable(
                    context,
                    if (isSaved) {
                        R.drawable.ic_round_library_add_check_24
                    } else R.drawable.ic_library_add_24dp
                )
                setOnClickListener {
                    media.media?.let { m ->
                        val finalMedia = Show(
                            id = m.id,
                            name = m.name ?: "",
                            media_type = m.media_type ?: "tv",
                            poster_path = m.poster_path
                        )

                        if (isSaved) {
                            mediaViewModel.deleteShow(finalMedia)
                        } else mediaViewModel.saveShow(finalMedia)

                        val status = if (isSaved) "removed from" else "added to"
                        val name = finalMedia.name
                        val snackBar = Snackbar.make(
                            binding.root,
                            "$name $status your library",
                            Snackbar.LENGTH_SHORT
                        )
                        snackBar.anchorView = binding.toolbarTv.root
                        snackBar.show()
                    }
                }
            }
        })

    }

    private fun setupMovieDatabaseObserver(media: MediaArgs) {
        mediaViewModel.getMovie(media.tmdbId).observe(viewLifecycleOwner, { isSaved ->
            Log.d("getMovie", "isSaved=$isSaved")
            binding.toolbarMovie.btnWatchlist.apply {
                icon = ContextCompat.getDrawable(
                    context,
                    if (isSaved) {
                        R.drawable.ic_round_library_add_check_24
                    } else R.drawable.ic_library_add_24dp
                )
                setOnClickListener {
                    media.media?.let { m ->
                        val finalMovie = Movie(
                            id = m.id,
                            title = m.title ?: "",
                            media_type = m.media_type ?: "movie",
                            poster_path = m.poster_path
                        )
                        if (isSaved) {
                            mediaViewModel.deleteMovie(finalMovie)
                        } else mediaViewModel.saveMovie(finalMovie)

                        val status = if (isSaved) "removed from" else "added to"
                        val name = finalMovie.title
                        val snackBar = Snackbar.make(
                            binding.root,
                            "$name $status your library",
                            Snackbar.LENGTH_SHORT
                        )
                        snackBar.anchorView = binding.toolbarMovie.root
                        snackBar.show()
                    }
                }
            }
        })

    }

    private fun setupMediaViewModel() {
        mediaViewModel.media.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    TransitionManager.beginDelayedTransition(binding.successView)
                    response.data?.let { doOnMediaSuccess(it) }
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        val errorMsg = if (message.isEmpty()) {
                            resources.getString(R.string.something_went_wrong)
                        } else message
                        Log.e(thisTAG, errorMsg)
                        binding.apply {
                            pbTv.isGone = true
                            successView.isGone = true
                            errorView.root.isVisible = true
                        }
                        binding.errorView.apply {
                            errorTxt.text = errorMsg
                        }
                    }
                }
                is Resource.Loading -> {
                    binding.apply {
                        successView.removeAllViews()
                        successView.recycledViewPool.clear()
                        pbTv.isVisible = true
                        successView.isGone = true
                        binding.errorView.root.isGone = true
                        toolbarTv.apply {
                            btnShare.setOnClickListener(null)
                        }
                        toolbarMovie.apply {
                            btnShare.setOnClickListener(null)
                        }
                    }
                }
            }
        })
    }

    private fun doOnMediaSuccess(
        response: MediaResponse
    ) {
        binding.apply {
            toolbarMovie.btnShare.setOnClickListener { shareIntent(mediaArgs) }
            toolbarTv.btnShare.setOnClickListener { shareIntent(mediaArgs) }
        }

        setupMediaAdapterOnClickListener(response)

        val metaData = listOf(
            MediaDataModel.Meta(
                title = response.name ?: "",
                mediaType = mediaArgs.mediaType,
                overview = response.overview,
                posterUrl = response.poster_path,
                tmdbId = response.id,
                misc = response.misc,
            )
        )

        val detailsList = mutableListOf<MediaDataModel.Details>()

        if (response.seasons.isNotEmpty()) {
            val seasonsList = response.seasons.map {
                AboutDataModel.Season(
                    episode_count = it.episode_count,
                    id = it.id,
                    name = it.name,
                    poster_path = it.poster_path,
                    season_number = it.season_number
                )
            }
            detailsList.add(
                MediaDataModel.Details(
                    header = resources.getString(R.string.seasons),
                    items = seasonsList
                )
            )
        }

        if (response.related_media.isNotEmpty()) {
            val relatedList = response.related_media.map {
                AboutDataModel.Curation(
                    id = it.id,
                    media_type = it.media_type,
                    name = it.name,
                    poster_path = it.poster_path,
                    title = it.title,
                    vote_average = it.vote_average
                )
            }
            detailsList.add(
                MediaDataModel.Details(
                    header = resources.getString(R.string.related_movies),
                    items = relatedList
                )
            )
        }


        if (response.cast.isNotEmpty()) {
            val castsList = response.cast.map {
                AboutDataModel.Cast(
                    character = it.character,
                    credit_id = it.credit_id,
                    person_id = it.id,
                    name = it.name,
                    profile_path = it.profile_path
                )
            }

            detailsList.add(
                MediaDataModel.Details(
                    header = resources.getString(R.string.cast),
                    items = castsList
                )
            )
        }


        if (response.similar.isNotEmpty()) {
            val similarList = response.similar.map {
                AboutDataModel.Curation(
                    id = it.id,
                    media_type = it.media_type,
                    name = it.name,
                    poster_path = it.poster_path,
                    title = it.title,
                    vote_average = it.vote_average
                )
            }
            detailsList.add(
                MediaDataModel.Details(
                    header = resources.getString(R.string.similar),
                    items = similarList
                )
            )
        }

        if (response.recommendations.isNotEmpty()) {
            val recommendationsList = response.recommendations.map {
                AboutDataModel.Curation(
                    id = it.id,
                    media_type = it.media_type,
                    name = it.name,
                    poster_path = it.poster_path,
                    title = it.title,
                    vote_average = it.vote_average
                )
            }
            detailsList.add(
                MediaDataModel.Details(
                    header = resources.getString(R.string.recommendations),
                    items = recommendationsList
                )
            )
        }

        if (response.videos.isNotEmpty()) {
            val videosList = response.videos.map {
                AboutDataModel.Video(
                    name = it.name,
                    key = it.key,
                    site = it.site,
                    thumbUrl = it.thumbUrl,
                    watchUrl = it.watchUrl
                )
            }
            detailsList.add(
                MediaDataModel.Details(
                    header = resources.getString(R.string.related_videos),
                    items = videosList
                )
            )
        }

        val finalList = metaData + detailsList
        mediaDataAdapter.differ.submitList(finalList.toList())

        binding.apply {
            pbTv.isVisible = false
            successView.isGone = false
            binding.errorView.root.isGone = true
        }

    }

    private fun setupMediaAdapterOnClickListener(
        response: MediaResponse
    ) {
        mediaDataAdapter.setOnItemClickListener {
            when (it) {
                is AboutDataModel.Season -> {
                    seasonViewModel.setShowSeason(
                        driveId = driveId,
                        tmdbId = response.id,
                        seasonName = it.name,
                        seasonNumber = it.season_number,
                        showName = response.name ?: "Unknown"
                    )
                    findNavController().navigate(R.id.action_fragmentMedia_to_episodeListFragment)
                }
                is AboutDataModel.Curation -> {
                    val media = Media(
                        id = it.id,
                        media_type = it.media_type,
                        name = it.name,
                        poster_path = it.poster_path,
                        title = it.title,
                        vote_average = it.vote_average
                    )
                    if (it.media_type != null) {
                        showsViewModel.setMedia(it.id, it.media_type, media)
                    } else showsViewModel.setMedia(it.id, mediaArgs.mediaType, media)
                }
                is AboutDataModel.Video -> {
                    val openVideoIntent = Intent(
                        Intent.ACTION_VIEW, Uri.parse(it.watchUrl)
                    )
                    startActivity(openVideoIntent)
                }
                is AboutDataModel.Cast -> {
                    castDetailsViewModel.setCast(
                        it.person_id, it.credit_id
                    )
                    findNavController().navigate(R.id.action_fragmentMedia_to_castsFragment)
                }
                is AboutDataModel.Header -> {
                    if (it.heading != null && it.heading.startsWith("/")) {
                        bigImageViewModel.setImagePath(it.heading)
                        findNavController().navigate(R.id.action_fragmentMedia_to_bigImageFragment)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.successView.apply {
            adapter = mediaDataAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
            itemAnimator = null
        }
    }

    private fun shareIntent(media: MediaArgs) {
        val showName = media.media?.name ?: media.media?.title
        val mediaType = media.mediaType
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "https://www.themoviedb.org/${mediaType}/${media.tmdbId}"
        )
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, showName)
        startActivity(shareIntent)
    }

    private fun searchQuery(
        tmdbId: Int, mediaType: String
    ): String {
        val lookInFolder = when (mediaType) {
            "tv" -> ZPLEX_SHOWS_ID
            "movie" -> ZPLEX_MOVIES_ID
            else -> ""
        }
        return "name contains '${tmdbId}' and parents in '${lookInFolder}' and trashed = false"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        driveId = null
        binding.apply {
            successView.recycledViewPool.clear()
            successView.adapter = null
        }
        _binding = null
    }
}
