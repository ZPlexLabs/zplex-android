package zechs.zplex.ui.fragment.discover

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import zechs.zplex.R
import zechs.zplex.adapter.browse.BrowseDataAdapter
import zechs.zplex.adapter.browse.BrowseDataModel
import zechs.zplex.databinding.FragmentDiscoverBinding
import zechs.zplex.models.dataclass.GenreList
import zechs.zplex.ui.fragment.viewmodels.FiltersViewModel

class DiscoverFragment : Fragment(R.layout.fragment_discover) {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private val filterModel by activityViewModels<FiltersViewModel>()
    private val browseAdapter by lazy { BrowseDataAdapter() }
    private val thisTag = "DiscoverFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDiscoverBinding.bind(view)

        setupRecyclerView()
        addData()
    }

    private fun addData() {
        val dataList = listOf(
            BrowseDataModel.Meta(title = "Search"),
            BrowseDataModel.Browse(
                header = "Browse shows",
                genres = tvGenreList()
            ),
            BrowseDataModel.Browse(
                header = "Browse movies",
                genres = movieGenreList()
            )
        )
        browseAdapter.differ.submitList(dataList)
    }

    private fun movieGenreList(): List<GenreList> {
        return listOf(
            GenreList(28, "Action", "movie", getIcon(R.drawable.ic_action)),
            GenreList(12, "Adventure", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(16, "Animation", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(35, "Comedy", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(80, "Crime", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(99, "Documentary", "movie", getIcon(R.drawable.ic_documentary)),
            GenreList(18, "Drama", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(10751, "Family", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(14, "Fantasy", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(36, "History", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(27, "Horror", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(10402, "Music", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(9648, "Mystery", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(10749, "Romance", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(878, "Sci-Fi", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(10770, "TV Movie", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(53, "Thriller", "movie", getIcon(R.drawable.ic_thriller)),
            GenreList(10752, "War", "movie", getIcon(R.drawable.ic_theater)),
            GenreList(37, "Western", "movie", getIcon(R.drawable.ic_theater))
        )
    }

    private fun tvGenreList(): List<GenreList> {
        return listOf(
            GenreList(10759, "Action", "tv", getIcon(R.drawable.ic_action)),
            GenreList(16, "Animation", "tv", getIcon(R.drawable.ic_theater)),
            GenreList(35, "Comedy", "tv", getIcon(R.drawable.ic_theater)),
            GenreList(80, "Crime", "tv", getIcon(R.drawable.ic_theater)),
            GenreList(99, "Documentary", "tv", getIcon(R.drawable.ic_theater)),
            GenreList(18, "Drama", "tv", getIcon(R.drawable.ic_theater)),
            GenreList(10751, "Family", "tv", getIcon(R.drawable.ic_theater)),
            GenreList(10762, "Kids", "tv", getIcon(R.drawable.ic_theater)),
            GenreList(9648, "Mystery", "tv", getIcon(R.drawable.ic_theater)),
            GenreList(10763, "News", "tv", getIcon(R.drawable.ic_theater)),
            GenreList(10764, "Reality", "tv", getIcon(R.drawable.ic_theater)),
            GenreList(10765, "Sci-Fi", "tv", getIcon(R.drawable.ic_theater)),
            GenreList(10768, "War & Politics", "tv", getIcon(R.drawable.ic_theater)),
            GenreList(37, "Western", "tv", getIcon(R.drawable.ic_theater))
        )
    }

    private fun getIcon(icon: Int): Drawable? {
        return ContextCompat.getDrawable(requireContext(), icon)
    }

    private fun setupRecyclerView() {
        binding.successView.apply {
            adapter = browseAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
            itemAnimator = null
        }

        browseAdapter.setOnItemClickListener {
            Log.d(thisTag, it.toString())
            if (it.id == 0) {
                findNavController().navigate(R.id.action_discoverFragment_to_searchFragment)
            } else {
                it.mediaType?.let { mediaType ->
                    filterModel.setFilter(
                        mediaType = mediaType,
                        sortBy = "popularity.desc",
                        page = 1,
                        withKeyword = null,
                        withGenres = it.id
                    )
                    findNavController().navigate(R.id.action_discoverFragment_to_browseFragment)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            successView.adapter = null
        }
        _binding = null
    }
}