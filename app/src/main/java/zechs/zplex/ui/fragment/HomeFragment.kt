package zechs.zplex.ui.fragment

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_home.*
import zechs.zplex.R
import zechs.zplex.adapter.FilesAdapter
import zechs.zplex.adapter.LogsAdapter
import zechs.zplex.ui.FileViewModel
import zechs.zplex.ui.ReleaseLogViewModel
import zechs.zplex.ui.activity.AboutActivity
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.utils.Constants.Companion.ZPLEX
import zechs.zplex.utils.Resource
import java.net.IDN
import java.net.URI
import java.net.URL


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: FileViewModel
    private lateinit var viewModel2: FileViewModel

    private lateinit var filesAdapter: FilesAdapter
    private lateinit var filesAdapter2: FilesAdapter

    private lateinit var logsViewModel: ReleaseLogViewModel
    private lateinit var logsAdapter: LogsAdapter

    private val TAG = "HomeFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as ZPlexActivity).viewModel
        viewModel2 = (activity as ZPlexActivity).viewModel
        logsViewModel = (activity as ZPlexActivity).logsViewModel

        setupRecyclerView()
        appBarLayout.setPadding(0, getStatusBarHeight(), 0, 0)

        viewModel.filesList.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { filesResponse ->
                        filesAdapter.differ.submitList(filesResponse.files.toList())
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(
                            context,
                            "An error occurred: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "An error occurred: $message")
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        logsViewModel.logsList.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    TransitionManager.beginDelayedTransition(root)
                    response.data?.let { logsResponse ->
                        logsAdapter.differ.submitList(logsResponse.releasesLog.toList())
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(
                            context,
                            "An error occurred: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "An error occurred: $message")
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        viewModel2.getSavedFiles().observe(viewLifecycleOwner, { files ->
            filesAdapter2.differ.submitList(files)
        })
    }

    private fun hideProgressBar() {
        loadingHome.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        loadingHome.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        filesAdapter = FilesAdapter()
        filesAdapter2 = FilesAdapter()

        rvHome.apply {
            adapter = filesAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }

        filesAdapter.setOnItemClickListener {
            val posterUrl = Uri.parse("${ZPLEX}${it.name}/poster.jpg")
            val name = it.name.split(" - ").toTypedArray()[0]
            val type = it.name.split(" - ").toTypedArray()[1]
            val intent = Intent(activity, AboutActivity::class.java)
            intent.putExtra("NAME", name)
            intent.putExtra("TYPE", type)
            intent.putExtra("POSTERURL", posterUrl.toString())

            val bundle = Bundle().apply {
                putSerializable("file", it)
            }

            intent.putExtras(bundle)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
        }

        logsAdapter = LogsAdapter()
        rvNewEpisodes.apply {
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
            )

            try {

                println(playURI.toASCIIString())
                val vlcIntent = Intent(Intent.ACTION_VIEW)
                vlcIntent.setPackage("org.videolan.vlc")
                vlcIntent.component = ComponentName(
                    "org.videolan.vlc",
                    "org.videolan.vlc.gui.video.VideoPlayerActivity"
                )
                vlcIntent.setDataAndTypeAndNormalize(Uri.parse(playURI.toASCIIString()), "video/*")
                vlcIntent.putExtra("title", it.file.dropLast(4))
                vlcIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                startActivity(vlcIntent)
            } catch (notFoundException: ActivityNotFoundException) {
                notFoundException.printStackTrace()
                Toast.makeText(
                    context,
                    "VLC not found, Install VLC from Play Store",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        rvMyShowsHome.apply {
            adapter = filesAdapter2
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            filesAdapter2.setOnItemClickListener {
                val posterUrl = Uri.parse("${ZPLEX}${it.name}/poster.jpg")

                val name = it.name.split(" - ").toTypedArray()[0]
                val type = it.name.split(" - ").toTypedArray()[1]
                val intent = Intent(activity, AboutActivity::class.java)
                intent.putExtra("NAME", name)
                intent.putExtra("TYPE", type)
                intent.putExtra("POSTERURL", posterUrl.toString())

                val bundle = Bundle().apply {
                    putSerializable("file", it)
                }

                intent.putExtras(bundle)

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                startActivity(intent)
//                activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)

                findNavController().navigate(R.id.action_homeFragment_to_aboutFragment)
            }
        }
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}