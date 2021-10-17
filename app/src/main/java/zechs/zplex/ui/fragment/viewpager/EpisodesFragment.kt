package zechs.zplex.ui.fragment.viewpager

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zechs.zplex.R
import zechs.zplex.adapter.MediaAdapter
import zechs.zplex.databinding.FragmentEpisodesBinding
import zechs.zplex.models.drive.DriveResponse
import zechs.zplex.models.drive.File
import zechs.zplex.ui.activity.PlayerActivity
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.AboutFragmentArgs
import zechs.zplex.ui.viewmodel.file.FileViewModel
import zechs.zplex.utils.Constants.Companion.ZPLEX
import zechs.zplex.utils.Resource
import java.lang.Integer.parseInt
import java.net.*

class EpisodesFragment(
    file: File,
    args: AboutFragmentArgs
) : Fragment(R.layout.fragment_episodes) {

    private var _binding: FragmentEpisodesBinding? = null
    private val binding get() = _binding!!

    private lateinit var fileViewModel: FileViewModel
    private lateinit var mediaAdapter: MediaAdapter

    private lateinit var groupedList: Map<String, List<File>>
    private val thisTAG = "EpisodesFragment"

    private val seriesId = args.seriesId
    private val type = args.type
    private val name = args.name
    private val id = file.id

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEpisodesBinding.bind(view)

        fileViewModel = (activity as ZPlexActivity).viewModel

        setupRecyclerView(isReversed = false, loadBig = true)
        filesLoading()

        val driveQuery =
            "name contains 'mkv' and '${id}' in parents and trashed = false"

        binding.btnRetryEpisodes.setOnClickListener {
            fileViewModel.getMediaFiles(driveQuery)
        }

        fileViewModel.getMediaFiles(driveQuery)

        fileViewModel.mediaList.observe(viewLifecycleOwner, { responseMedia ->
            when (responseMedia) {
                is Resource.Success -> {
                    responseMedia.data?.let { filesResponse ->
                        filesSuccess(filesResponse)
                    }
                }

                is Resource.Error -> {
                    filesError(responseMedia)
                }

                is Resource.Loading -> {
                    filesLoading()
                }
            }
        })

    }

    private fun filesSuccess(filesResponse: DriveResponse) {

        if (filesResponse.files.isNotEmpty()) {
            val filesList = filesResponse.files.toList()

            groupedList = filesList.groupBy {
                val season = it.name.take(3)
                val first = season.take(1).replace("S", "Season ")
                val count = parseInt(season.drop(1))
                first + count
            }

            var seasonIndex = 0
            val seasons = groupedList.keys.toList()
            mediaAdapter.differ.submitList(groupedList[seasons[seasonIndex]]?.toList())

            binding.apply {
                rvEpisodes.visibility = View.VISIBLE
                btnRetryEpisodes.visibility = View.GONE
                pbEpisodes.visibility = View.GONE
            }

            val filters = listOf("Chronological", "Newest Aired")
            context?.let { context ->
                val listPopUpSeasons =
                    ListPopupWindow(
                        context,
                        null,
                        R.attr.listPopupWindowStyle
                    )
                val listPopUpFilters =
                    ListPopupWindow(
                        context,
                        null,
                        R.attr.listPopupWindowStyle
                    )
                binding.seasonsMenu.apply {

                    visibility = View.VISIBLE
                    text = seasons[seasonIndex]

                    val adapter = ArrayAdapter(
                        context,
                        R.layout.item_dropdown,
                        seasons
                    )

                    listPopUpSeasons.anchorView = this
                    listPopUpSeasons.setAdapter(adapter)

                    setOnClickListener { listPopUpSeasons.show() }

                    listPopUpSeasons.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                        seasonIndex = position
                        mediaAdapter.differ.submitList(groupedList[seasons[seasonIndex]]?.toList())
                        text = seasons[seasonIndex]
                        binding.rvEpisodes.smoothScrollToPosition(0)
                        listPopUpSeasons.dismiss()
                    }

                }

                var loadBig = true
                var reverseList = false

                binding.toggleView.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
//                        val transition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
//                        transition.duration = 500
//                        transition.excludeTarget(toolbar, true)
//                        transition.excludeTarget(android.R.id.statusBarBackground, true)
//                        transition.excludeTarget(android.R.id.navigationBarBackground, true)
//                        TransitionManager.beginDelayedTransition(binding.root, transition)

                        if (loadBig) {
                            loadBig = false
                            icon = ContextCompat.getDrawable(context, R.drawable.toggle_grid)
                        } else {
                            loadBig = true
                            icon = ContextCompat.getDrawable(context, R.drawable.toggle_list)
                        }

                        setupRecyclerView(reverseList, loadBig)
                        mediaAdapter.differ.submitList(groupedList[seasons[seasonIndex]]?.toList())
                        binding.rvEpisodes.smoothScrollToPosition(0)

                    }
                }


                binding.filterEps.apply {

                    visibility = View.VISIBLE
                    text = filters[0]

                    val adapter = ArrayAdapter(
                        context,
                        R.layout.item_dropdown,
                        filters
                    )

                    listPopUpFilters.anchorView = this
                    listPopUpFilters.setAdapter(adapter)

                    setOnClickListener { listPopUpFilters.show() }

                    listPopUpFilters.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                        reverseList = when (position) {
                            0 -> false
                            1 -> true
                            else -> false
                        }
                        setupRecyclerView(
                            reverseList, loadBig
                        )
                        mediaAdapter.differ.submitList(groupedList[seasons[seasonIndex]]?.toList())
                        binding.rvEpisodes.smoothScrollToPosition(0)
                        text = filters[position]
                        listPopUpFilters.dismiss()
                    }
                }
            }
        }
    }


    private fun filesLoading() {
        mediaAdapter.differ.submitList(listOf<File>().toList())

        val emptyList = listOf<String>().toList()

        context?.let {
            val adapter =
                ArrayAdapter(
                    it,
                    R.layout.item_dropdown,
                    emptyList
                )

            val listPopupWindow =
                ListPopupWindow(
                    it,
                    null,
                    R.attr.listPopupWindowStyle
                )
            listPopupWindow.anchorView = binding.seasonsMenu
            listPopupWindow.setAdapter(adapter)
            binding.seasonsMenu.text = ""
            binding.filterEps.text = ""
        }

        binding.apply {
            rvEpisodes.visibility = View.INVISIBLE
            btnRetryEpisodes.visibility = View.INVISIBLE
            pbEpisodes.visibility = View.VISIBLE
            seasonsMenu.visibility = View.INVISIBLE
            filterEps.visibility = View.INVISIBLE
            toggleView.visibility = View.GONE
        }
    }

    private fun filesError(responseMedia: Resource.Error<DriveResponse>) {
        binding.apply {
            rvEpisodes.visibility = View.INVISIBLE
            btnRetryEpisodes.visibility = View.VISIBLE
            pbEpisodes.visibility = View.INVISIBLE
            seasonsMenu.visibility = View.INVISIBLE
            filterEps.visibility = View.INVISIBLE
        }
        responseMedia.message?.let { message ->
            makeText(
                context,
                "An error occurred: $message",
                Toast.LENGTH_SHORT
            ).show()
            Log.e(thisTAG, "An error occurred: $message")
        }
    }

    private fun setupRecyclerView(isReversed: Boolean, loadBig: Boolean) {
        mediaAdapter = MediaAdapter(seriesId, loadBig)

        mediaAdapter.setOnItemClickListener {
            playMedia(it)
        }

        binding.rvEpisodes.apply {
            adapter = mediaAdapter
            layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, isReversed)
        }
    }

    private fun playMedia(it: File) {
        val items = arrayOf("ExoPlayer", "VLC")

        context?.let { it1 ->
            MaterialAlertDialogBuilder(it1)
                .setBackground(
                    ContextCompat.getDrawable(
                        it1,
                        R.drawable.popup_menu_bg
                    )
                ).setTitle("Play using")
                .setItems(items) { dialog, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(activity, PlayerActivity::class.java)
                            intent.putExtra("fileId", it.id)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            activity?.startActivity(intent)
                            dialog.dismiss()
                        }
                        1 -> {
                            val playUrl =
                                if (type == "TV") "${ZPLEX}${seriesId} - $name - TV/${it.name}" else "${ZPLEX}${name}"
                            try {
                                val episodeURL = URL(playUrl)
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
                                vlcIntent.putExtra("title", it.name.dropLast(4))
                                vlcIntent.flags =
                                    FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                                requireContext().startActivity(vlcIntent)
                            } catch (notFoundException: ActivityNotFoundException) {
                                notFoundException.printStackTrace()
                                makeText(
                                    context,
                                    "VLC not found, Install VLC from Play Store",
                                    LENGTH_LONG
                                ).show()
                            } catch (e: MalformedURLException) {
                                e.printStackTrace()
                                makeText(
                                    context,
                                    e.localizedMessage,
                                    LENGTH_LONG
                                ).show()
                            } catch (e: URISyntaxException) {
                                e.printStackTrace()
                                makeText(
                                    context,
                                    e.localizedMessage,
                                    LENGTH_LONG
                                ).show()
                            }
                            dialog.dismiss()
                        }
                    }
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaAdapter.differ.submitList(listOf<File>().toList())
        binding.rvEpisodes.adapter = null
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }
}