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
import zechs.zplex.R
import zechs.zplex.adapter.FilesAdapter
import zechs.zplex.databinding.FragmentMyShowsBinding
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.viewmodel.file.FileViewModel


class MyShowsFragment : Fragment(R.layout.fragment_my_shows) {

    private lateinit var viewModel: FileViewModel
    private lateinit var filesAdapter: FilesAdapter

    private var _binding: FragmentMyShowsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyShowsBinding.bind(view)

        viewModel = (activity as ZPlexActivity).viewModel
        setupRecyclerView()
//        binding.rvMyShows.setPadding(0, getStatusBarHeight() + 24, 0, 0)

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
                val position = viewHolder.absoluteAdapterPosition
                val file = filesAdapter.differ.currentList[position]
                viewModel.deleteFile(file)
                Snackbar.make(view, "Successfully removed", Snackbar.LENGTH_LONG).show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rvMyShows)
        }
    }

    private fun setupRecyclerView() {
        filesAdapter = FilesAdapter()
        binding.rvMyShows.apply {
            adapter = filesAdapter
            layoutManager = GridLayoutManager(activity, 3)
            filesAdapter.setOnItemClickListener {
                try {
                    val seriesId = (it.name.split(" - ").toTypedArray()[0]).toInt()
                    val name = it.name.split(" - ").toTypedArray()[1]
                    val type = it.name.split(" - ").toTypedArray()[2]

                    val action = MyShowsFragmentDirections.actionMyShowsFragmentToAboutFragment(
                        it,
                        seriesId,
                        type,
                        name,
                    )
                    findNavController().navigate(action)
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, "TVDB id not found", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvMyShows.adapter = null
        _binding = null
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