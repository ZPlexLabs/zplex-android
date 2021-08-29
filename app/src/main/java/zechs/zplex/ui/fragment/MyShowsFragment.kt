package zechs.zplex.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_my_shows.*
import zechs.zplex.R
import zechs.zplex.adapter.FilesAdapter
import zechs.zplex.ui.FileViewModel
import zechs.zplex.ui.activity.AboutActivity
import zechs.zplex.ui.activity.ZPlexActivity


class MyShowsFragment : Fragment(R.layout.fragment_my_shows) {

    private lateinit var viewModel: FileViewModel
    private lateinit var filesAdapter: FilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
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
                val posterUrl =
                    Uri.parse("https://zplex.zechs.workers.dev/0:/${it.name}/poster.jpg")
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