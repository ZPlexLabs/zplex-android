package zechs.zplex.ui.fragment.episodes

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.adapter.EpisodesAdapter
import zechs.zplex.databinding.FragmentEpisodeBinding
import zechs.zplex.models.season.Ep
import zechs.zplex.models.season.Seasons
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.ui.activity.PlayerActivity
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.EpisodeViewModel
import zechs.zplex.ui.fragment.SeasonViewModel
import zechs.zplex.ui.fragment.image.BigImageViewModel
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp
import zechs.zplex.utils.Resource
import java.net.*


class EpisodesFragment : Fragment(R.layout.fragment_episode) {

    private var _binding: FragmentEpisodeBinding? = null
    private val binding get() = _binding!!

    private val episodeViewModel by activityViewModels<EpisodeViewModel>()
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
                seasonNumber = showSeason.seasonNumber,
                driveId = showSeason.driveId
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


    private fun doOnSuccess(seasonResponse: Seasons) {

        val posterUrl = if (seasonResponse.poster_path == null) {
            R.drawable.no_poster
        } else {
            "${TMDB_IMAGE_PREFIX}/${PosterSize.w500}${seasonResponse.poster_path}"
        }

        episodesAdapter.setOnItemClickListener {
            episodeViewModel.setShowEpisode(
                tmdbId = tmdbId,
                seasonNumber = it.season_number ?: 0,
                episodeNumber = it.episode_number ?: 0
            )
            findNavController().navigate(R.id.action_episodesListFragment_to_watchFragment)
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

        val episodesList = seasonResponse.episodes.toList()
        episodesAdapter.differ.submitList(episodesList)

        if (episodesList.isEmpty()) {
            val errorMsg = "Episode list is empty..."
            Log.e(thisTAG, errorMsg)
            binding.apply {
                appBarLayout.isInvisible = true
                rvEpisodes.isInvisible = true
                pbEpisodes.isInvisible = true
                errorView.root.isVisible = true
            }
            binding.errorView.apply {
                errorTxt.text = errorMsg
            }
        } else {
            binding.apply {
                appBarLayout.isVisible = true
                rvEpisodes.isVisible = true
                pbEpisodes.isInvisible = true
                errorView.root.isVisible = false
            }
        }

    }

    private fun playMedia(it: Ep) {
        val items = arrayOf("ExoPlayer", "VLC")
        context?.let { c ->
            val roundedBg = ContextCompat.getDrawable(
                c, R.drawable.popup_menu_bg
            )
            MaterialAlertDialogBuilder(c)
                .setBackground(roundedBg)
                .setTitle("Play using")
                .setItems(items) { dialog, which ->
                    doWhich(dialog, which, it)
                }.show()
        }
    }

    private fun doWhich(dialog: DialogInterface, which: Int, it: Ep) {
        val fullEpisodeTitle = if (it.fileName.length > 30) {
            it.name?.dropLast(4)
        } else {
            "${it.fileName} - ${it.name?.dropLast(4)}"
        }

        when (which) {
            0 -> {
                val intent = Intent(activity, PlayerActivity::class.java)
                intent.putExtra("fileId", it.fileId)
                intent.putExtra("title", fullEpisodeTitle)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                activity?.startActivity(intent)
                dialog.dismiss()
            }
            1 -> {
                // val playUrl = "${Constants.ZPLEX}${mediaId} - $name - TV/${it.name}"
                playVLC(fullEpisodeTitle)
                dialog.dismiss()
            }
        }
    }

    private fun playVLC(fullEpisodeTitle: String?) {
        try {
            val episodeURL = URL("playUrl")
            val episodeURI = URI(
                episodeURL.protocol,
                episodeURL.userInfo,
                IDN.toASCII(episodeURL.host),
                episodeURL.port,
                episodeURL.path,
                episodeURL.query,
                episodeURL.ref
            ).toASCIIString().replace("?", "%3F")

            val vlcIntent = Intent(Intent.ACTION_VIEW)
            vlcIntent.setPackage("org.videolan.vlc")
            vlcIntent.component = ComponentName(
                "org.videolan.vlc",
                "org.videolan.vlc.gui.video.VideoPlayerActivity"
            )
            vlcIntent.setDataAndTypeAndNormalize(
                Uri.parse(episodeURI),
                "video/*"
            )
            vlcIntent.putExtra("title", fullEpisodeTitle)
            vlcIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            requireContext().startActivity(vlcIntent)
        } catch (notFoundException: ActivityNotFoundException) {
            notFoundException.printStackTrace()
            Toast.makeText(
                context,
                "VLC not found, Install VLC from Play Store",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            Toast.makeText(
                context, e.localizedMessage, Toast.LENGTH_LONG
            ).show()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            Toast.makeText(
                context, e.localizedMessage, Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.rvEpisodes.adapter = null
        _binding = null
    }
}