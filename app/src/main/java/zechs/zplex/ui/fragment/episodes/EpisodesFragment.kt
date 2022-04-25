package zechs.zplex.ui.fragment.episodes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Transition
import zechs.zplex.R
import zechs.zplex.adapter.EpisodesAdapter
import zechs.zplex.databinding.FragmentEpisodeBinding
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.models.zplex.Episode
import zechs.zplex.ui.BaseFragment
import zechs.zplex.ui.activity.main.MainActivity
import zechs.zplex.ui.activity.player.PlayerActivity
import zechs.zplex.ui.fragment.image.BigImageViewModel
import zechs.zplex.ui.fragment.shared_viewmodels.SeasonViewModel
import zechs.zplex.ui.seasonResponseZplex
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp
import zechs.zplex.utils.Resource


class EpisodesFragment : BaseFragment() {

    override val enterTransitionListener: Transition.TransitionListener? = null

    private var _binding: FragmentEpisodeBinding? = null
    private val binding get() = _binding!!

    private val seasonViewModel by activityViewModels<SeasonViewModel>()
    private val bigImageViewModel: BigImageViewModel by activityViewModels()
    private lateinit var episodesViewModel: EpisodesViewModel
    private val episodesAdapter by lazy { EpisodesAdapter() }

//    private var episode: Episode? = null
//    private val streamsDialog by lazy {
//        StreamsDialog(
//            requireContext(),
//            onStreamClick = {
//                episode?.let { e -> onStreamClick(it, e) }
//            },
//            onDownloadClick = {
//                episode?.let { e -> onDownloadClick(it, e, requireContext()) }
//            }
//        )
//    }

    private val thisTAG = "EpisodesFragment"
    private var tmdbId = 0
    private var showName: String? = null
    private var showPoster: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEpisodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEpisodeBinding.bind(view)

        episodesViewModel = (activity as MainActivity).episodesViewModel

        binding.rvEpisodes.apply {
            adapter = episodesAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
            itemAnimator = null
        }

        binding.seasonToolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        seasonViewModel.showId.observe(viewLifecycleOwner) { showSeason ->
            showName = showSeason.showName
            showPoster = showSeason.showPoster
            episodesViewModel.zplexGetSeason(
                tvId = showSeason.tmdbId,
                seasonNumber = showSeason.seasonNumber
            )

            val posterUrl = if (showSeason?.posterPath == null) {
                R.drawable.no_poster
            } else {
                "$TMDB_IMAGE_PREFIX/${PosterSize.w500}${showSeason.posterPath}"
            }

            val seasonText = "Season ${showSeason.seasonNumber}"

            binding.apply {
                showSeason.seasonName?.let {
                    if (it == seasonText) {
                        seasonToolbar.title = seasonText
                        seasonToolbar.subtitle = it
                    } else {
                        seasonToolbar.title = seasonText
                        seasonToolbar.subtitle = it
                    }
                }

                GlideApp.with(ivPoster)
                    .load(posterUrl)
                    .placeholder(R.drawable.no_poster)
                    .into(ivPoster)

                ivPoster.setOnClickListener {
                    openImageFullSize(showSeason?.posterPath, binding.ivPoster)
                }

            }

            tmdbId = showSeason.tmdbId
        }

//        setupDashStreamsObserver()

        episodesViewModel.seasonZplex.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                handleZplexEpisodeResponse(response)
            }
        }
    }

    private fun handleZplexEpisodeResponse(response: Resource<seasonResponseZplex>) {
        when (response) {
            is Resource.Success -> {
                response.data?.let { seasonResponse ->
                    doOnSuccess(seasonResponse)
                }
            }

            is Resource.Error -> {
                response.message?.let { message ->
                    val errorMsg = message.ifEmpty {
                        resources.getString(R.string.something_went_wrong)
                    }
                    Log.e(thisTAG, errorMsg)
                    binding.apply {
                        appBarLayout.isInvisible = true
                        rvEpisodes.isInvisible = true
                        pbEpisodes.isVisible = true
                    }
                    binding.errorView.apply {
                        errorTxt.text = errorMsg
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
                episodesAdapter.setOnItemClickListener { _, _ -> }
            }

            is Resource.Loading -> {
                binding.apply {
                    rvEpisodes.isInvisible = true
                    pbEpisodes.isVisible = true
                    errorView.root.isVisible = false
                }
                episodesAdapter.setOnItemClickListener { _, _ -> }
            }
        }
    }

    private fun openImageFullSize(posterPath: String?, imageView: ImageView) {
        imageView.transitionName = posterPath
        this.exitTransition = null
        bigImageViewModel.setImagePath(posterPath)

        val action = EpisodesFragmentDirections.actionEpisodesListFragmentToBigImageFragment()
        val extras = FragmentNavigatorExtras(
            imageView to imageView.transitionName
        )
        findNavController().navigate(action, extras)
        Log.d("navigateToMedia", imageView.transitionName)
    }


    private fun doOnSuccess(seasonResponse: seasonResponseZplex) {

        val episodesList = seasonResponse.episodes?.toList() ?: listOf()
        episodesAdapter.differ.submitList(episodesList)

        binding.apply {
            seasonResponse.name.let {
                val seasonText = "Season ${seasonResponse.season_number}"

                if (it == seasonText) {
                    seasonToolbar.title = seasonText
                    seasonToolbar.subtitle = it
                } else {
                    seasonToolbar.title = seasonText
                    seasonToolbar.subtitle = it
                }
            }

            val posterUrl = if (seasonResponse.poster_path == null) {
                R.drawable.no_poster
            } else {
                "$TMDB_IMAGE_PREFIX/${PosterSize.w500}${seasonResponse.poster_path}"
            }

            GlideApp.with(ivPoster)
                .load(posterUrl)
                .placeholder(R.drawable.no_poster)
                .into(ivPoster)

            ivPoster.setOnClickListener {
                openImageFullSize(seasonResponse.poster_path, binding.ivPoster)
            }
        }

        if (episodesList.isEmpty()) {
            val errorMsg = getString(R.string.no_episodes_found)
            Log.e(thisTAG, errorMsg)
            binding.apply {
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
                rvEpisodes.isVisible = true
                pbEpisodes.isInvisible = true
                errorView.root.isVisible = false
            }
        }

        episodesAdapter.setOnItemClickListener { episode: Episode, isLast: Boolean ->
            //it.fileId?.let { it1 -> episodesViewModel.getVideoInfo(it1) }
            //context?.let { c -> showStreamsDialog(c, it) }
            playEpisode(episode, seasonResponse.accessToken!!, isLast)
        }
    }

//    private fun showStreamsDialog(context: Context, e: Episode) {
//        episode = e
//        streamsDialog.show()
//
//        streamsDialog.window?.apply {
//            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            setLayout(
//                Constraints.LayoutParams.MATCH_PARENT,
//                Constraints.LayoutParams.WRAP_CONTENT
//            )
//        }
//
//        val streamsView = streamsDialog.findViewById<RecyclerView>(R.id.rv_streams)
//
//        streamsView.apply {
//            adapter = streamsDialog.streamsDataAdapter
//            layoutManager = LinearLayoutManager(
//                context, LinearLayoutManager.VERTICAL, false
//            )
//        }
//
//        val streamsList = mutableListOf<StreamsDataModel>()
//        e.fileId?.let { id ->
//            streamsList.add(
//                StreamsDataModel.Original(
//                    title = "Original",
//                    id = id
//                )
//            )
//            episodesViewModel.getVideoInfo(e.fileId)
//        }
//
//        streamsList.add(StreamsDataModel.Loading)
//
//        streamsDialog.streamsDataAdapter.differ.submitList(streamsList.toList())
//    }

//    private fun onStreamClick(it: StreamsDataModel, episode: Episode) {
//        when (it) {
//            is StreamsDataModel.Original -> {
//                playEpisode(episode, null, null)
//                streamsDialog.dismiss()
//            }
//            is StreamsDataModel.Stream -> {
//                playEpisode(episode, it.cookie, it.url)
//                streamsDialog.dismiss()
//            }
//            else -> {}
//        }
//    }

    private fun getDriveDownloadUrl(fileId: String): String {
        return "https://www.googleapis.com/drive/v3/files/${fileId}?supportAllDrives=True&alt=media"
    }

//    private fun onDownloadClick(it: StreamsDataModel, episode: Episode, context: Context) {
//        when (it) {
//            is StreamsDataModel.Original -> {
//                val outputPath = "$showName/${episode.fileName!!}"
//                queueDownload(
//                    context,
//                    getDriveDownloadUrl(episode.fileId!!), outputPath,
//                    "Authorization",
//                    "Bearer ${SessionManager(context).fetchAuthToken()}"
//                )
//                streamsDialog.dismiss()
//            }
//            is StreamsDataModel.Stream -> {
//                val outputPath = "$showName/${episode.fileName!!}"
//                queueDownload(
//                    context, it.url, outputPath,
//                    "Cookie",
//                    "DRIVE_STREAM=${it.cookie}"
//                )
//                streamsDialog.dismiss()
//            }
//            else -> {}
//        }
//    }

    private fun playEpisode(
        episode: Episode, accessToken: String, isLastEpisode: Boolean
    ) {
        episode.file_id?.let { id ->
            val title = if (episode.name.isEmpty()) {
                "No title"
            } else "Episode ${episode.episode_number} - ${episode.name}"
            val intent = Intent(activity, PlayerActivity::class.java)
            intent.putExtra("fileId", id)
            intent.putExtra("title", title)
            intent.putExtra("accessToken", accessToken)
            intent.putExtra("tmdbId", tmdbId)
            intent.putExtra("name", showName!!)
            intent.putExtra("posterPath", showPoster)
            intent.putExtra("seasonNumber", episode.season_number)
            intent.putExtra("episodeNumber", episode.episode_number)
            intent.putExtra("isTV", true)
            intent.putExtra("isLastEpisode", isLastEpisode)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            activity?.startActivity(intent)
        }
    }

//    private fun setupDashStreamsObserver() {
//        episodesViewModel.dashVideo.observe(viewLifecycleOwner) { event ->
//            event.getContentIfNotHandled()?.let { streamsResponse ->
//                handleDashStreamsResponse(streamsResponse)
//            }
//        }
//    }
//
//    private fun handleDashStreamsResponse(
//        streamsResponse: Resource<DashResponse>
//    ) {
//        when (streamsResponse) {
//            is Resource.Success -> {
//                val data = streamsResponse.data
//                if (data != null) {
//                    handleStreamsSuccess(data)
//                }
//                data?.error?.let {
//                    Toast.makeText(
//                        requireContext(),
//                        it, Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//            else -> {
//                val adapterDiff = streamsDialog.streamsDataAdapter.differ
//                val currentList = adapterDiff.currentList
//                val streamsList = mutableListOf<StreamsDataModel>()
//                streamsList.add(currentList.filterIsInstance<StreamsDataModel.Original>()[0])
//                adapterDiff.submitList(streamsList.toList())
//            }
//        }
//    }
//
//    private fun handleStreamsSuccess(dashResponse: DashResponse) {
//        val streams = dashResponse.streams
//        val adapterDiff = streamsDialog.streamsDataAdapter.differ
//        val currentList = adapterDiff.currentList
//
//        val streamsList = mutableListOf<StreamsDataModel>()
//        streamsList.add(currentList.filterIsInstance<StreamsDataModel.Original>()[0])
//
//        for (stream in streams) {
//            streamsList.add(
//                StreamsDataModel.Stream(
//                    name = stream.quality,
//                    url = stream.url,
//                    cookie = stream.driveStream
//                )
//            )
//
//        }
//        adapterDiff.submitList(streamsList.toList())
//    }

//    private fun queueDownload(
//        context: Context,
//        downloadUrl: String,
//        outputPath: String,
//        headerKey: String,
//        headerValue: String
//    ) {
//        if (hasPermission()) {
//            val request = DownloadManager.Request(Uri.parse(downloadUrl))
//                .addRequestHeader(headerKey, headerValue)
//                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
//                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//                .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, outputPath)
//
//            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//            dm.enqueue(request)
//            Toast.makeText(context, getString(R.string.download_started), Toast.LENGTH_SHORT).show()
//        } else {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ), 1
//            )
//            Toast.makeText(
//                context,
//                getString(R.string.storage_permission_request),
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
//
//    private fun hasPermission(): Boolean = ActivityCompat.checkSelfPermission(
//        requireContext(),
//        Manifest.permission.READ_EXTERNAL_STORAGE
//    ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
//        requireContext(),
//        Manifest.permission.WRITE_EXTERNAL_STORAGE
//    ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        super.onDestroy()
        binding.rvEpisodes.adapter = null
        _binding = null
    }
}