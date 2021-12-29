package zechs.zplex.ui.fragment.myshows

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import zechs.zplex.R
import zechs.zplex.adapter.SearchAdapter
import zechs.zplex.databinding.FragmentMyShowsBinding
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.viewmodels.ShowViewModel


class MyShowsFragment : Fragment(R.layout.fragment_my_shows) {

    private var _binding: FragmentMyShowsBinding? = null
    private val binding get() = _binding!!

    private lateinit var myShowsViewModel: MyShowsViewModel
    private val showsViewModel by activityViewModels<ShowViewModel>()

    private val showsAdapter by lazy { SearchAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyShowsBinding.bind(view)

        myShowsViewModel = (activity as ZPlexActivity).myShowsViewModel
        setupRecyclerView()

        myShowsViewModel.savedMedia.observe(viewLifecycleOwner, { media ->
            val isEmpty = media?.isEmpty() ?: true
            binding.rvMyShows.isGone = isEmpty
            binding.errorView.apply {
                root.isVisible = isEmpty
                retryBtn.isVisible = false
                errorTxt.text = getString(R.string.your_library_is_empty)
                errorIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_no_shows_24
                    )
                )
            }
            showsAdapter.differ.submitList(media)
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
                val media = showsAdapter.differ.currentList[position]
                val bottomNavView = activity?.findViewById(
                    R.id.bottomNavigationView
                ) as BottomNavigationView?
                if (media.media_type == "tv") {
                    val show = Show(
                        id = media.id,
                        name = media.name ?: "",
                        media_type = media.media_type,
                        poster_path = media.poster_path,
                        vote_average = media.vote_average
                    )
                    myShowsViewModel.deleteShow(show)
                } else {
                    val movie = Movie(
                        id = media.id,
                        title = media.title ?: "",
                        media_type = media.media_type,
                        poster_path = media.poster_path,
                        vote_average = media.vote_average
                    )
                    myShowsViewModel.deleteMovie(movie)
                }
                val name = media.name ?: media.title
                val snackBar = Snackbar.make(
                    view, "$name removed from your library",
                    Snackbar.LENGTH_SHORT
                )
                bottomNavView?.let {
                    snackBar.anchorView = it
                }
                snackBar.show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvMyShows)
    }

    private fun setupRecyclerView() {
        binding.rvMyShows.apply {
            adapter = showsAdapter
            layoutManager = GridLayoutManager(activity, 3)
            showsAdapter.setOnItemClickListener { media ->
                if (media.media_type != null) {
                    showsViewModel.setMedia(media.id, media.media_type, media)
                } else showsViewModel.setMedia(media.id, "none", media)

                findNavController().navigate(R.id.action_myShowsFragment_to_fragmentMedia)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvMyShows.adapter = null
        _binding = null
    }

}