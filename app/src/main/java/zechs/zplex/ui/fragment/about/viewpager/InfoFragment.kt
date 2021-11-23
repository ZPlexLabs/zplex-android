package zechs.zplex.ui.fragment.about.viewpager

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialFade
import zechs.zplex.R
import zechs.zplex.adapter.MiscAdapter
import zechs.zplex.databinding.FragmentInfoBinding
import zechs.zplex.models.misc.Pairs
import zechs.zplex.models.tmdb.movies.MoviesResponse
import zechs.zplex.models.tvdb.series.SeriesResponse
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.about.AboutViewModel
import zechs.zplex.utils.Resource

class InfoFragment : Fragment(R.layout.fragment_info) {

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var aboutViewModel: AboutViewModel

    private lateinit var miscAdapter: MiscAdapter
    private val thisTAG = "InfoFragment"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInfoBinding.bind(view)

        aboutViewModel = (activity as ZPlexActivity).aboutViewModel

        miscAdapter = MiscAdapter()

        binding.listMisc.apply {
            adapter = miscAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        aboutViewModel.series.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let {
                        tvdbSuccess(it)
                    }
                }

                is Resource.Error -> {
                    response.message?.let { message ->
                        apiError(message)
                    }
                }
                is Resource.Loading ->
                    apiLoading()
            }
        })

        aboutViewModel.movies.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let {
                        tmdbSuccess(it)
                    }
                }

                is Resource.Error -> {
                    response.message?.let { message ->
                        apiError(message)
                    }
                }
                is Resource.Loading -> {
                    apiLoading()
                }
            }
        })
    }

    private fun tmdbSuccess(it: MoviesResponse) {
        val materialFade = MaterialFade().apply {
            duration = 150L
        }
        TransitionManager.beginDelayedTransition(binding.root, materialFade)

        val pairsArray: MutableList<Pairs> = mutableListOf()

        it.vote_average?.let {
            pairsArray.add(Pairs("Rating", it.toString()))
        }

        it.release_date?.let {
            pairsArray.add(Pairs("Released", it))
        }

        it.runtime?.let {
            pairsArray.add(Pairs("Released", "$it min"))
        }

        if (it.production_companies != null
            &&
            it.production_companies.isNotEmpty()
        ) {
            val companies = mutableListOf<String>()
            for (company in it.production_companies) {
                company.name?.let { it1 -> companies.add(it1) }
            }
            pairsArray.add(
                Pairs("Countries", companies.joinToString(separator = ", "))
            )
        }

        if (it.production_countries != null
            &&
            it.production_countries.isNotEmpty()
        ) {
            val countries = mutableListOf<String>()
            for (country in it.production_countries) {
                country.name?.let { it1 -> countries.add(it1) }
            }
            pairsArray.add(
                Pairs("Countries", countries.joinToString(separator = ", "))
            )
        }

        miscAdapter.differ.submitList(pairsArray.toList())

        binding.seriesInfo.visibility = View.VISIBLE
        binding.tvTitle.text = it.title

        it.genres?.forEach { genre ->
            val mChip = layoutInflater.inflate(
                R.layout.item_chip,
                binding.root,
                false
            ) as Chip
            mChip.text = genre.name
            binding.cgGenre.addView(mChip)
        }

        it.overview?.let { plot ->
            binding.tvPlot.apply {
                if (plot.length > 175) {
                    val trimPlot = plot.substring(0, 175) + "..."
                    text = trimPlot
                    setOnClickListener {
                        val materialFade = MaterialFade().apply {
                            duration = 150L
                        }
                        TransitionManager.beginDelayedTransition(binding.root, materialFade)
                        text = if (text.length > 178) trimPlot else plot
                    }
                } else {
                    text = plot
                    setOnClickListener(null)
                }
            }
        }
    }


    private fun tvdbSuccess(seriesResponse: SeriesResponse) {
        val materialFade = MaterialFade().apply {
            duration = 150L
        }
        TransitionManager.beginDelayedTransition(binding.root, materialFade)

        seriesResponse.data?.let {

            val pairsArray: MutableList<Pairs> = mutableListOf()

            it.network?.let { network ->
                pairsArray.add(Pairs("Network", network))
            }

            it.rating?.let { rating ->
                pairsArray.add(Pairs("Rating", rating))
            }

            it.firstAired?.let { firstAired ->
                pairsArray.add(Pairs("Released", firstAired.take(4)))
            }

            it.runtime?.let { runtime ->
                pairsArray.add(Pairs("Runtime", "$runtime min"))
            }

            miscAdapter.differ.submitList(pairsArray)

            binding.seriesInfo.visibility = View.VISIBLE
            binding.tvTitle.text = it.seriesName

            it.genre?.forEach { text ->
                val mChip = layoutInflater.inflate(
                    R.layout.item_chip,
                    binding.root,
                    false
                ) as Chip
                mChip.text = text
                binding.cgGenre.addView(mChip)

            }

            it.overview?.let { plot ->
                binding.tvPlot.apply {
                    if (plot.length > 225) {
                        val trimPlot = plot.substring(0, 225) + "..."
                        text = trimPlot
                        setOnClickListener {
                            val materialFade = MaterialFade().apply {
                                duration = 150L
                            }
                            TransitionManager.beginDelayedTransition(binding.root, materialFade)
                            text =
                                if (text.length > 228) trimPlot else plot
                        }
                    } else {
                        text = plot
                        setOnClickListener(null)
                    }
                }
            }

        }

    }

    private fun apiError(message: String?) {
        binding.seriesInfo.visibility = View.INVISIBLE
        Toast.makeText(
            context,
            "An error occurred: $message",
            Toast.LENGTH_SHORT
        ).show()
        Log.e(thisTAG, "An error occurred: $message")
    }

    private fun apiLoading() {
        binding.apply {
            cgGenre.removeAllViews()
            seriesInfo.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

