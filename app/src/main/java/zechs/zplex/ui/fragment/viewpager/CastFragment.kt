package zechs.zplex.ui.fragment.viewpager

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import zechs.zplex.R
import zechs.zplex.adapter.ActorsAdapter
import zechs.zplex.adapter.CreditsAdapter
import zechs.zplex.databinding.FragmentCastsBinding
import zechs.zplex.models.tmdb.credits.Cast
import zechs.zplex.models.tvdb.actors.Data
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.viewmodel.tmdb.TmdbViewModel
import zechs.zplex.ui.viewmodel.tvdb.TvdbViewModel
import zechs.zplex.utils.Resource

class CastFragment(
    private val isTV: Boolean,
    private val mediaId: Int,
) : Fragment(R.layout.fragment_casts) {

    private var _binding: FragmentCastsBinding? = null
    private val binding get() = _binding!!

    private lateinit var tvdbViewModel: TvdbViewModel
    private lateinit var tmdbViewModel: TmdbViewModel

    private lateinit var creditsAdapter: CreditsAdapter
    private lateinit var actorsAdapter: ActorsAdapter

    private val thisTAG = "CastFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCastsBinding.bind(view)

        tvdbViewModel = (activity as ZPlexActivity).tvdbViewModel
        tmdbViewModel = (activity as ZPlexActivity).tmdbViewModel

        setupRecyclerView(isTV)
        isLoading(true)

        binding.btnRetryCasts.setOnClickListener {
            if (isTV) {
                tvdbViewModel.getActor(mediaId)
            } else {
                tmdbViewModel.getCredits(mediaId)
            }
        }

        if (isTV) {

            tvdbViewModel.getActor(mediaId)

            tvdbViewModel.actors.observe(viewLifecycleOwner, { responseMedia ->
                when (responseMedia) {
                    is Resource.Success -> {
                        responseMedia.data?.let {
                            isLoading(false)
                            actorsAdapter.differ.submitList(it.data?.toList())
                        }
                    }

                    is Resource.Error -> {
                        binding.apply {
                            pbCasts.visibility = View.VISIBLE
                            btnRetryCasts.visibility = View.VISIBLE
                            rvCasts.visibility = View.GONE
                        }
                        responseMedia.message?.let { message ->
                            Toast.makeText(
                                context,
                                "An error occurred: $message",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(thisTAG, "An error occurred: $message")
                        }
                    }

                    is Resource.Loading -> {
                        actorsAdapter.differ.submitList(listOf<Data>().toList())
                        isLoading(true)
                    }
                }
            })
        } else {

            tmdbViewModel.getCredits(mediaId)

            tmdbViewModel.credits.observe(viewLifecycleOwner, { responseMedia ->
                when (responseMedia) {
                    is Resource.Success -> {
                        isLoading(false)
                        responseMedia.data?.let {
                            creditsAdapter.differ.submitList(it.cast?.toList())
                        }
                    }

                    is Resource.Error -> {
                        binding.apply {
                            pbCasts.visibility = View.VISIBLE
                            btnRetryCasts.visibility = View.VISIBLE
                            rvCasts.visibility = View.GONE
                        }
                        responseMedia.message?.let { message ->
                            Toast.makeText(
                                context,
                                "An error occurred: $message",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(thisTAG, "An error occurred: $message")
                        }
                    }

                    is Resource.Loading -> {
                        creditsAdapter.differ.submitList(listOf<Cast>().toList())
                        isLoading(true)
                    }
                }
            })
        }

    }

    private fun isLoading(isLoading: Boolean) {
        binding.apply {
            pbCasts.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
            rvCasts.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
            btnRetryCasts.visibility = View.INVISIBLE
        }
    }

    private fun setupRecyclerView(isTV: Boolean) {
        if (isTV) {
            actorsAdapter = ActorsAdapter()

            binding.rvCasts.apply {
                adapter = actorsAdapter
                layoutManager = GridLayoutManager(activity, 2)
            }
        } else {
            creditsAdapter = CreditsAdapter()

            binding.rvCasts.apply {
                adapter = creditsAdapter
                layoutManager = GridLayoutManager(activity, 2)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            pbCasts.visibility = View.INVISIBLE
            rvCasts.visibility = View.INVISIBLE
            btnRetryCasts.visibility = View.INVISIBLE
        }
        binding.rvCasts.adapter = null
        _binding = null
    }
}