package zechs.zplex.ui.fragment.discover

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import zechs.zplex.R
import zechs.zplex.adapter.browse.BrowseDataAdapter
import zechs.zplex.adapter.browse.BrowseDataModel
import zechs.zplex.databinding.FragmentDiscoverBinding
import zechs.zplex.models.tmdb.genre.Genre
import zechs.zplex.ui.fragment.FiltersViewModel

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

    private fun movieGenreList(): List<Genre> {
        return listOf(
            Genre(id = 28, name = "Action", mediaType = "movie"),
            Genre(id = 12, name = "Adventure", mediaType = "movie"),
            Genre(id = 16, name = "Animation", mediaType = "movie"),
            Genre(id = 35, name = "Comedy", mediaType = "movie"),
            Genre(id = 80, name = "Crime", mediaType = "movie"),
            Genre(id = 99, name = "Documentary", mediaType = "movie"),
            Genre(id = 18, name = "Drama", mediaType = "movie"),
            Genre(id = 10751, name = "Family", mediaType = "movie"),
            Genre(id = 14, name = "Fantasy", mediaType = "movie"),
            Genre(id = 36, name = "History", mediaType = "movie"),
            Genre(id = 27, name = "Horror", mediaType = "movie"),
            Genre(id = 10402, name = "Music", mediaType = "movie"),
            Genre(id = 9648, name = "Mystery", mediaType = "movie"),
            Genre(id = 10749, name = "Romance", mediaType = "movie"),
            Genre(id = 878, name = "Sci-Fi", mediaType = "movie"),
            Genre(id = 10770, name = "TV Movie", mediaType = "movie"),
            Genre(id = 53, name = "Thriller", mediaType = "movie"),
            Genre(id = 10752, name = "War", mediaType = "movie"),
            Genre(id = 37, name = "Western", mediaType = "movie")
        )
    }

    private fun tvGenreList(): List<Genre> {
        return listOf(
            Genre(id = 10759, name = "Action", mediaType = "tv"),
            Genre(id = 16, name = "Animation", mediaType = "tv"),
            Genre(id = 35, name = "Comedy", mediaType = "tv"),
            Genre(id = 80, name = "Crime", mediaType = "tv"),
            Genre(id = 99, name = "Documentary", mediaType = "tv"),
            Genre(id = 18, name = "Drama", mediaType = "tv"),
            Genre(id = 10751, name = "Family", mediaType = "tv"),
            Genre(id = 10762, name = "Kids", mediaType = "tv"),
            Genre(id = 9648, name = "Mystery", mediaType = "tv"),
            Genre(id = 10763, name = "News", mediaType = "tv"),
            Genre(id = 10764, name = "Reality", mediaType = "tv"),
            Genre(id = 10765, name = "Sci-Fi", mediaType = "tv"),
            Genre(id = 10766, name = "Soap", mediaType = "tv"),
            Genre(id = 10767, name = "Talk", mediaType = "tv"),
            Genre(id = 10768, name = "War & Politics", mediaType = "tv"),
            Genre(id = 37, name = "Western", mediaType = "tv")
        )
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
                filterModel.setFilter(
                    mediaType = it.mediaType,
                    sortBy = "popularity.desc",
                    page = 1,
                    withKeyword = null,
                    withGenres = it.id
                )
                findNavController().navigate(R.id.action_discoverFragment_to_browseFragment)
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