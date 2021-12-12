package zechs.zplex.ui.fragment.myshows

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.adapter.FilesAdapter
import zechs.zplex.databinding.FragmentMyShowsBinding
import zechs.zplex.models.Args
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.ArgsViewModel


class MyShowsFragment : Fragment(R.layout.fragment_my_shows) {

    private var _binding: FragmentMyShowsBinding? = null
    private val binding get() = _binding!!

    private val argsModel: ArgsViewModel by activityViewModels()

    private lateinit var myShowsViewModel: MyShowsViewModel
    private lateinit var filesAdapter: FilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFade()
        exitTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, true
        ).apply {
            duration = 500L
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyShowsBinding.bind(view)

        myShowsViewModel = (activity as ZPlexActivity).myShowsViewModel
        setupRecyclerView()

        myShowsViewModel.getSavedShows().observe(viewLifecycleOwner, { files ->
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
                myShowsViewModel.deleteShow(file)
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
                    findNavController().navigate(R.id.action_myShowsFragment_to_aboutFragment)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvMyShows.adapter = null
        _binding = null
    }

}