package zechs.zplex.ui.fragment.media

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.adapter.media.AboutDataModel
import zechs.zplex.adapter.media.MediaDataAdapter
import zechs.zplex.adapter.media.MediaDataModel
import zechs.zplex.databinding.FragmentMediaBinding
import zechs.zplex.models.dataclass.MediaArgs
import zechs.zplex.models.tmdb.media.MediaResponse
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.image.BigImageViewModel
import zechs.zplex.ui.fragment.viewmodels.CastDetailsViewModel
import zechs.zplex.ui.fragment.viewmodels.SeasonViewModel
import zechs.zplex.ui.fragment.viewmodels.ShowViewModel
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
    private val tempDriveId = "1pm1ng68XiIM8GWwLwqYH_4mdcFQMAcjW"
    private var mediaArgs = MediaArgs(0, "none")

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
            if (media.mediaType == "tv" || media.mediaType == "movie") {
                mediaViewModel.getMedia(media.tmdbId, media.mediaType)
                binding.errorView.retryBtn.setOnClickListener {
                    mediaViewModel.getMedia(media.tmdbId, media.mediaType)
                }
            } else {
                val errorMsg = "Unknown media type..."
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
            mediaArgs = MediaArgs(media.tmdbId, media.mediaType)
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
                        pbTv.isVisible = true
                        successView.isGone = true
                        binding.errorView.root.isGone = true
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
                        driveId = tempDriveId,
                        tmdbId = response.id,
                        seasonName = it.name,
                        seasonNumber = it.season_number,
                        showName = response.name ?: "Unknown"
                    )
                    findNavController().navigate(R.id.action_fragmentMedia_to_episodeListFragment)
                }
                is AboutDataModel.Curation -> {
                    if (it.media_type != null) {
                        showsViewModel.setMedia(it.id, it.media_type)
                    } else showsViewModel.setMedia(it.id, mediaArgs.mediaType)
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
                    bigImageViewModel.setImagePath(it.heading)
                    findNavController().navigate(R.id.action_fragmentMedia_to_bigImageFragment)
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            successView.adapter = null
        }
        _binding = null
    }
}