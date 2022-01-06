package zechs.zplex.ui.fragment.home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import zechs.zplex.R
import zechs.zplex.adapter.SearchAdapter
import zechs.zplex.databinding.FragmentHomeBinding
import zechs.zplex.models.dataclass.MediaArgs
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.utils.Resource


class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    private val discoverMoviesAdapter by lazy { SearchAdapter() }
    private val discoverShowsAdapter by lazy { SearchAdapter() }
    private val discoverAnimeAdapter by lazy { SearchAdapter() }
    private val trendingAdapter by lazy { SearchAdapter() }

    private val thisTAG = "HomeFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        homeViewModel = (activity as ZPlexActivity).homeViewModel
        setupRecyclerView()
        homeViewModel.trending.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        textView2.isVisible = true
                        rvTrending.isVisible = true
                    }
                    response.data?.let { moviesResponse ->
                        trendingAdapter.differ.submitList(moviesResponse.results.toList())
                    }
                }
                is Resource.Error -> {
                    binding.apply {
                        textView2.isVisible = false
                        rvTrending.isVisible = false
                    }
                    response.message?.let { message ->
                        val errorText = "Unable to fetch trending. An error occurred: $message"
                        Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
                        Log.e(thisTAG, errorText)
                    }
                }
                is Resource.Loading -> {
                    binding.apply {
                        textView2.isVisible = false
                        rvTrending.isVisible = false
                    }
                }
            }
        })

        homeViewModel.movies.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        textView3.isVisible = true
                        rvMovies.isVisible = true
                    }
                    response.data?.let { moviesResponse ->
                        discoverMoviesAdapter.differ.submitList(moviesResponse.results.toList())
                    }
                }
                is Resource.Error -> {
                    binding.apply {
                        textView3.isVisible = false
                        rvMovies.isVisible = false
                    }
                    response.message?.let { message ->
                        val errorText =
                            "Unable to fetch popular movies. An error occurred: $message"
                        Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
                        Log.e(thisTAG, errorText)
                    }
                }
                is Resource.Loading -> {
                    binding.apply {
                        textView3.isVisible = false
                        rvMovies.isVisible = false
                    }
                }
            }
        })

        homeViewModel.shows.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        textView4.isVisible = true
                        rvShows.isVisible = true
                    }
                    response.data?.let { showsResponse ->
                        discoverShowsAdapter.differ.submitList(showsResponse.results.toList())
                    }
                }
                is Resource.Error -> {
                    binding.apply {
                        textView4.isVisible = false
                        rvShows.isVisible = false
                    }
                    response.message?.let { message ->
                        val errorText =
                            "Unable to fetch popular shows. An error occurred: $message"
                        Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
                        Log.e(thisTAG, errorText)
                    }
                }
                is Resource.Loading -> binding.apply {
                    textView4.isVisible = false
                    rvShows.isVisible = false
                }

            }
        })

        homeViewModel.animes.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        textView5.isVisible = true
                        rvAnime.isVisible = true
                    }
                    response.data?.let { animeResponse ->
                        discoverAnimeAdapter.differ.submitList(animeResponse.results.toList())
                    }
                }
                is Resource.Error -> {
                    binding.apply {
                        textView5.isVisible = false
                        rvAnime.isVisible = false
                    }
                    response.message?.let { message ->
                        val errorText =
                            "Unable to fetch popular animes. An error occurred: $message"
                        Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
                        Log.e(thisTAG, errorText)
                    }
                }
                is Resource.Loading -> {
                    binding.apply {
                        textView5.isVisible = false
                        rvAnime.isVisible = false
                    }
                }
            }
        })
    }

    private fun setupRecyclerView() {

        binding.rvTrending.apply {
            adapter = trendingAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.HORIZONTAL, false
            )
        }

        binding.rvMovies.apply {
            adapter = discoverMoviesAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.HORIZONTAL, false
            )
        }

        binding.rvShows.apply {
            adapter = discoverShowsAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.HORIZONTAL, false
            )
        }

        binding.rvAnime.apply {
            adapter = discoverAnimeAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.HORIZONTAL, false
            )
        }

        trendingAdapter.setOnItemClickListener { navigateToMedia(it, it.media_type ?: "tv") }
        discoverMoviesAdapter.setOnItemClickListener { navigateToMedia(it, "movie") }
        discoverShowsAdapter.setOnItemClickListener { navigateToMedia(it, "tv") }
        discoverAnimeAdapter.setOnItemClickListener { navigateToMedia(it, "tv") }
        discoverAnimeAdapter.setOnItemClickListener { navigateToMedia(it, "tv") }
    }

    private fun navigateToMedia(media: Media, mediaType: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToFragmentMedia(
            MediaArgs(media.id, mediaType, media)
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            rvMovies.adapter = null
            rvShows.adapter = null
            rvAnime.adapter = null
        }
        _binding = null
    }

}