package zechs.zplex.ui.fragment.myshows

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Transition
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import zechs.zplex.R
import zechs.zplex.adapter.SearchAdapter
import zechs.zplex.databinding.FragmentMyShowsBinding
import zechs.zplex.models.dataclass.MediaArgs
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.ui.BaseFragment
import zechs.zplex.ui.activity.ZPlexActivity


class MyShowsFragment : BaseFragment() {

    override val enterTransitionListener: Transition.TransitionListener? = null

    private var _binding: FragmentMyShowsBinding? = null
    private val binding get() = _binding!!

    private lateinit var myShowsViewModel: MyShowsViewModel
    private val showsAdapter by lazy { SearchAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyShowsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyShowsBinding.bind(view)


        myShowsViewModel = (activity as ZPlexActivity).myShowsViewModel
        setupRecyclerView()

        myShowsViewModel.savedMedia.observe(viewLifecycleOwner) { media ->
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
        }

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
                val media = showsAdapter.differ.currentList[position]
                val bottomNavView = activity?.findViewById(
                    R.id.bottomNavigationView
                ) as BottomNavigationView?
                val name = media.name ?: media.title
                val snackBar = Snackbar.make(
                    view, "$name removed from your library",
                    Snackbar.LENGTH_SHORT
                )
                if (media.media_type == "tv") {
                    val show = Show(
                        id = media.id,
                        name = media.name ?: "",
                        media_type = media.media_type,
                        poster_path = media.poster_path,
                        vote_average = media.vote_average
                    )
                    myShowsViewModel.deleteShow(show)
                    snackBar.setAction(
                        R.string.undo
                    ) {
                        myShowsViewModel.saveShow(show)
                    }
                } else {
                    val movie = Movie(
                        id = media.id,
                        title = media.title ?: "",
                        media_type = media.media_type,
                        poster_path = media.poster_path,
                        vote_average = media.vote_average
                    )
                    myShowsViewModel.deleteMovie(movie)
                    snackBar.setAction(R.string.undo) {
                        myShowsViewModel.saveMovie(movie)
                    }
                }

                bottomNavView?.let {
                    snackBar.anchorView = it
                }
                val accentColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorAccent
                    )
                )

                snackBar.setActionTextColor(accentColor)
                snackBar.show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvMyShows)
    }


    private fun setupRecyclerView() {
        binding.rvMyShows.apply {
            adapter = showsAdapter
            layoutManager = GridLayoutManager(activity, 3)
            showsAdapter.setOnItemClickListener { media, view, position ->
                val action = MyShowsFragmentDirections.actionMyShowsFragmentToFragmentMedia(
                    MediaArgs(media.id, media.media_type ?: "none", media, position)
                )

                val posterView = view.findViewById<MaterialCardView>(R.id.image_card)
                val extras = FragmentNavigatorExtras(posterView to posterView.transitionName)

                Log.d("showsAdapter", posterView.transitionName)

                findNavController().navigate(action, extras)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvMyShows.adapter = null
        _binding = null
    }

}