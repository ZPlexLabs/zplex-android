package zechs.zplex.ui.fragment.media

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
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

        showsViewModel.mediaArgs.observe(viewLifecycleOwner, { media ->
            val mediaType = if (media.mediaType == "none") "tv" else media.mediaType
            if (mediaType == "movie") {
                binding.apply {
                    toolbarMovie.root.isVisible = true
                    toolbarTv.root.isVisible = false
                }
                mediaViewModel.doSearchFor(searchQuery(media.tmdbId, media.mediaType))
            } else {
                binding.apply {
                    toolbarMovie.root.isVisible = false
                    toolbarTv.root.isVisible = true
                }
            }

            binding.toolbarTv.btnShare.setOnClickListener {
                shareIntent(media)
            }
            binding.toolbarMovie.btnShare.setOnClickListener {
                shareIntent(media)
            }

            if (mediaType == "tv" || mediaType == "movie") {
                mediaViewModel.getMedia(media.tmdbId, media.mediaType)
                binding.errorView.retryBtn.setOnClickListener {
                    mediaViewModel.getMedia(media.tmdbId, media.mediaType)
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
                            val finaMedia = m.copy(media_type = mediaType)

                            if (isSaved) {
                                mediaViewModel.deleteShow(finaMedia)
                            } else mediaViewModel.saveShow(finaMedia)

                            val status = if (isSaved) "removed from" else "added to"
                            val name = finaMedia.name ?: finaMedia.title
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

                binding.toolbarMovie.btnWatchlist.apply {
                    icon = ContextCompat.getDrawable(
                        context,
                        if (isSaved) {
                            R.drawable.ic_round_library_add_check_24
                        } else R.drawable.ic_library_add_24dp
                    )
                    setOnClickListener {
                        media.media?.let { m ->
                            val finaMedia = m.copy(media_type = mediaType)
                            if (isSaved) {
                                mediaViewModel.deleteShow(finaMedia)
                            } else mediaViewModel.saveShow(finaMedia)

                            val status = if (isSaved) "removed from" else "added to"
                            val name = finaMedia.name ?: finaMedia.title
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

            mediaArgs = MediaArgs(media.tmdbId, media.mediaType, media.media)
        })

        mediaViewModel.media.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
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
                        successView.recycledViewPool.clear()
                        pbTv.isVisible = true
                        successView.isGone = true
                        binding.errorView.root.isGone = true
                    }
                }
            }
        })

        mediaViewModel.searchList.observe(viewLifecycleOwner, { responseEpisode ->
            when (responseEpisode) {
                is Resource.Success -> {
                    responseEpisode.data?.let { driveResponse ->
                        binding.toolbarMovie.apply {
                            btnWatchNow.isInvisible = false
                            pbWatch.isVisible = false
                        }
                        if (driveResponse.files.isNotEmpty()) {
                            val file = driveResponse.files[0]
                            binding.toolbarMovie.btnWatchNow.apply {
                                text = resources.getString(R.string.watch_now)
                                icon = ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_round_play_circle_outline_24
                                )
                                setOnClickListener {
                                    val intent = Intent(activity, PlayerActivity::class.java)
                                    intent.putExtra("fileId", file.id)
                                    intent.putExtra("title", mediaArgs.media?.title)
                                    intent.flags = FLAG_ACTIVITY_NEW_TASK
                                    activity?.startActivity(intent)
                                }
                            }
                        } else {
                            binding.toolbarMovie.btnWatchNow.apply {
                                text = resources.getString(R.string.request_this_movie)
                                icon = ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_round_queue_play_next_24
                                )
                                setOnClickListener {
                                    Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    binding.toolbarMovie.apply {
                        btnWatchNow.isInvisible = true
                        pbWatch.isVisible = false
                    }
                }
                is Resource.Loading -> {
                    binding.toolbarMovie.apply {
                        btnWatchNow.isInvisible = true
                        pbWatch.isVisible = true
                    }
                }
            }
        })

    }


    private fun doOnMediaSuccess(response: MediaResponse) {

        mediaDataAdapter.setOnItemClickListener {
            when (it) {
                is AboutDataModel.Season -> {
                    seasonViewModel.setShowSeason(
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
                    it.heading?.let { text ->
                        if (text.startsWith("/")) {
                            bigImageViewModel.setImagePath(it.heading)
                            findNavController().navigate(R.id.action_fragmentMedia_to_bigImageFragment)
                        }
//                        else {
//                            mediaArgs.media?.let { media ->
//                                println(media)
//                                val name = media.name ?: media.title
//                                Snackbar.make(
//                                    view, "$name successfully saved.",
//                                    Snackbar.LENGTH_SHORT
//                                ).show()
//                                mediaViewModel.saveShow(media)
//                            }
//                        }
                    }
                }
                else -> {}
            }
        }

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
        binding.apply {
            successView.recycledViewPool.clear()
            successView.adapter = null
        }
        _binding = null
    }
}