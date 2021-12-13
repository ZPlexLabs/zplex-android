package zechs.zplex.ui.fragment.home

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFadeThrough
import zechs.zplex.R
import zechs.zplex.adapter.FilesAdapter
import zechs.zplex.adapter.LogsAdapter
import zechs.zplex.databinding.FragmentHomeBinding
import zechs.zplex.models.Args
import zechs.zplex.models.drive.File
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.ArgsViewModel
import zechs.zplex.utils.Constants.ZPLEX
import zechs.zplex.utils.Resource
import java.net.IDN
import java.net.URI
import java.net.URL


class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private val argsModel: ArgsViewModel by activityViewModels()

    private lateinit var filesAdapter: FilesAdapter
    private lateinit var filesAdapter2: FilesAdapter
    private lateinit var logsAdapter: LogsAdapter

    private val thisTAG = "HomeFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        homeViewModel = (activity as ZPlexActivity).homeViewModel
        setupRecyclerView()

        homeViewModel.homeList.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    homeView(visible = true)
                    response.data?.let { filesResponse ->
                        filesAdapter.differ.submitList(filesResponse.files.toList())
                    }
                }
                is Resource.Error -> {
                    homeView(visible = false)
                    response.message?.let { message ->
                        val errorText =
                            "Unable to fetch recently added shows. An error occurred: $message"
                        Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
                        Log.e(thisTAG, errorText)
                    }
                }
                is Resource.Loading -> {
                    homeView(visible = false)
                }
            }
        })

        homeViewModel.logsList.observe(viewLifecycleOwner, { response ->
            TransitionManager.beginDelayedTransition(binding.root)
            when (response) {
                is Resource.Success -> {
                    response.data?.let { logsResponse ->
                        val newEpisodeList = logsResponse.releasesLog.toList()
                        logView(visible = newEpisodeList.isNotEmpty())
                        logsAdapter.differ.submitList(newEpisodeList)
                    }
                }
                is Resource.Error -> {
                    logView(visible = false)
                    response.message?.let { message ->
                        val errorText =
                            "Unable to fetch new episodes list. An error occurred: $message"
                        Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
                        Log.e(thisTAG, errorText)
                    }
                }
                is Resource.Loading -> {
                    logView(visible = false)
                }
            }
        })

        homeViewModel.getSavedShows().observe(viewLifecycleOwner, { files ->
            showsView(visible = files.toList().isNotEmpty())
            if (files.toList().isNotEmpty()) filesAdapter2.differ.submitList(files)
        })
    }

    private fun homeView(visible: Boolean) {
        TransitionManager.beginDelayedTransition(binding.root, MaterialFadeThrough())
        binding.apply {
            recentlyAdded.isVisible = visible
            rvNewEpisodes.isVisible = visible
        }
    }

    private fun logView(visible: Boolean) {
        TransitionManager.beginDelayedTransition(binding.root, MaterialFadeThrough())
        binding.apply {
            newEpisodes.isVisible = visible
            rvHome.isVisible = visible
        }
    }

    private fun showsView(visible: Boolean) {
        binding.apply {
            myShows.isVisible = visible
            rvMyShowsHome.isVisible = visible
        }
    }

    private fun setupRecyclerView() {
        filesAdapter = FilesAdapter()
        filesAdapter2 = FilesAdapter()

        binding.rvHome.apply {
            adapter = filesAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }

        filesAdapter.setOnItemClickListener {
            getDetails(it)
        }

        logsAdapter = LogsAdapter()
        binding.rvNewEpisodes.apply {
            adapter = logsAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }

        logsAdapter.setOnItemClickListener {
            val playUrl = URL("${ZPLEX}${it.folder}/${it.file}")
            val playURI = URI(
                playUrl.protocol,
                playUrl.userInfo,
                IDN.toASCII(playUrl.host),
                playUrl.port,
                playUrl.path,
                playUrl.query,
                playUrl.ref
            ).toASCIIString().replace("?", "%3F")

            try {
                val vlcIntent = Intent(Intent.ACTION_VIEW)
                vlcIntent.setPackage("org.videolan.vlc")
                vlcIntent.component = ComponentName(
                    "org.videolan.vlc",
                    "org.videolan.vlc.gui.video.VideoPlayerActivity"
                )
                vlcIntent.setDataAndTypeAndNormalize(Uri.parse(playURI), "video/*")
                vlcIntent.putExtra("title", it.file.dropLast(4))
                vlcIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                startActivity(vlcIntent)
            } catch (notFoundException: ActivityNotFoundException) {
                notFoundException.printStackTrace()
                Toast.makeText(
                    context,
                    "VLC not found, Install VLC from Play Store",
                    LENGTH_LONG
                ).show()
            }
        }

        binding.rvMyShowsHome.apply {
            adapter = filesAdapter2
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            filesAdapter2.setOnItemClickListener {
                getDetails(it)
            }
        }
    }

    private fun getDetails(it: File) {
        val regex = "^(.*[0-9])( - )(.*)( - )(TV|Movie)".toRegex()
        val nameSplit = regex.find(it.name)?.destructured?.toList()

        if (nameSplit != null) {
            val mediaId = nameSplit[0]
            val mediaName = nameSplit[2]
            val mediaType = nameSplit[4]

            argsModel.setArg(
                Args(
                    file = it,
                    mediaId = mediaId.toInt(),
                    type = mediaType,
                    name = mediaName
                )
            )
            findNavController().navigate(R.id.action_homeFragment_to_aboutFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            rvHome.adapter = null
            rvNewEpisodes.adapter = null
            rvMyShowsHome.adapter = null
        }
        _binding = null
    }

}