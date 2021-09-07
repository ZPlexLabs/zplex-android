package zechs.zplex.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.android.synthetic.main.fragment_my_shows.*
import zechs.zplex.R
import zechs.zplex.adapter.FilesAdapter
import zechs.zplex.ui.FileViewModel
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.utils.Constants.Companion.ZPLEX
import java.net.IDN
import java.net.URI
import java.net.URL


class MyShowsFragment : Fragment(R.layout.fragment_my_shows) {

    private lateinit var viewModel: FileViewModel
    private lateinit var filesAdapter: FilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialFadeThrough()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as ZPlexActivity).viewModel
        setupRecyclerView()

        appBarLayout.setPadding(
            0, appBarLayout.paddingTop + getStatusBarHeight(), 0, 0
        )

        viewModel.getSavedFiles().observe(viewLifecycleOwner, { files ->
            filesAdapter.differ.submitList(files)
        })

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val file = filesAdapter.differ.currentList[position]
                viewModel.deleteFile(file)
                Snackbar.make(view, "Successfully removed", Snackbar.LENGTH_LONG).show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(rvMyShows)
        }
    }

    private fun setupRecyclerView() {
        filesAdapter = FilesAdapter()
        rvMyShows.apply {
            adapter = filesAdapter
            layoutManager = GridLayoutManager(activity, 3)
            filesAdapter.setOnItemClickListener {
                try {
                    val posterURL = URL("${ZPLEX}${it.name}/poster.jpg")
                    val posterUri = URI(
                        posterURL.protocol,
                        posterURL.userInfo,
                        IDN.toASCII(posterURL.host),
                        posterURL.port,
                        posterURL.path,
                        posterURL.query,
                        posterURL.ref
                    )
                    val seriesId = (it.name.split(" - ").toTypedArray()[0]).toInt()
                    val name = it.name.split(" - ").toTypedArray()[1]
                    val type = it.name.split(" - ").toTypedArray()[2]

                    val action = MyShowsFragmentDirections.actionMyShowsFragmentToAboutFragment(
                        it,
                        seriesId,
                        type,
                        name,
                        posterUri.toASCIIString()
                    )
                    findNavController().navigate(action)
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, "TVDB id not found", Toast.LENGTH_LONG).show()
                }
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