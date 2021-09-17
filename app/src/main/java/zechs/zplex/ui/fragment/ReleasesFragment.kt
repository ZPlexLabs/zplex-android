package zechs.zplex.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_releases.*
import zechs.zplex.R
import zechs.zplex.adapter.LogsAdapter
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.viewmodel.release_log.ReleaseLogViewModel
import zechs.zplex.utils.Constants
import zechs.zplex.utils.Resource


class ReleasesFragment : Fragment(R.layout.fragment_releases) {

    private lateinit var logsViewModel: ReleaseLogViewModel
    private lateinit var logsAdapter: LogsAdapter
    private val TAG = "HomeFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logsViewModel = (activity as ZPlexActivity).logsViewModel
        setupRecyclerView()

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
    }

    private fun hideProgressBar() {
        loadingReleases.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        loadingReleases.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        logsAdapter = LogsAdapter()
        rvReleases.apply {
            adapter = logsAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        logsAdapter.setOnItemClickListener {

            val show = it.folder.split(" - ", ignoreCase = false, limit = 2).toTypedArray()[0]
            val episode = it.file.split(" - ", ignoreCase = false, limit = 2).toTypedArray()[0]
            val episodeTitle =
                (it.file.split(" - ", ignoreCase = false, limit = 2).toTypedArray()[1]).dropLast(4)
            val posterUrl = Uri.parse("${Constants.ZPLEX}${show}/poster.jpg")

            val bodyText = try {
                "Season ${episode.substring(1, 3).toInt()}, Episode ${
                    episode.substring(4).toInt()
                } - $episodeTitle"
            } catch (nfe: NumberFormatException) {
                nfe.printStackTrace()
                "$episode - $episodeTitle"
            }

//            val intent = Intent(activity, AboutActivity::class.java)
//            intent.putExtra("NAME", show)
//            intent.putExtra("TYPE", "TV")
//            intent.putExtra("POSTERURL", posterUrl.toString())
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
//            activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
        }
    }
}