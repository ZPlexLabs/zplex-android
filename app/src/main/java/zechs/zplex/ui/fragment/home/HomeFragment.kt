package zechs.zplex.ui.fragment.home

import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.adapter.SearchAdapter
import zechs.zplex.databinding.FragmentHomeBinding
import zechs.zplex.models.dataclass.MediaArgs
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.models.tmdb.search.SearchResponse
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.utils.Resource


class HomeFragment : Fragment(R.layout.fragment_home) {

    private val thisTAG = "HomeFragment"

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private val trendingAdapter by lazy { SearchAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = TransitionSet().apply {
            addTransition(
                MaterialSharedAxis(
                    MaterialSharedAxis.Y, true
                ).apply {
                    interpolator = LinearInterpolator()
                    duration = 500
                })

            addTransition(Fade().apply {
                interpolator = LinearInterpolator()
            })
        }

        exitTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, true
        ).apply {
            interpolator = LinearInterpolator()
            duration = 500
        }

        returnTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, false
        ).apply {
            interpolator = LinearInterpolator()
            duration = 220
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        homeViewModel = (activity as ZPlexActivity).homeViewModel

        binding.btnGoSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        setupRecyclerView()
        setupTrendingObserver()
    }

    private fun setupTrendingObserver() {
        homeViewModel.trending.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { trendingResponse ->
                        trendingSuccess(trendingResponse)
                    }
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }

    }

    private fun trendingSuccess(listResponse: SearchResponse) {
        TransitionManager.beginDelayedTransition(binding.root)
        trendingAdapter.differ.submitList(listResponse.results.take(9).toList())
    }

    private fun setupRecyclerView() {

        binding.rvTrending.apply {
            adapter = trendingAdapter
            layoutManager = GridLayoutManager(activity, 3)
        }

        trendingAdapter.setOnItemClickListener { media, view, position ->
            navigateToMedia(media, media.media_type ?: "tv", view, position)
        }

    }

    private fun navigateToMedia(media: Media, mediaType: String, view: View, position: Int) {
        val action = HomeFragmentDirections.actionHomeFragmentToFragmentMedia(
            MediaArgs(media.id, mediaType, media, position)
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}