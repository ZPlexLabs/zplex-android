package zechs.zplex.ui.fragment.about.viewpager

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.chip.Chip
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

    private val limit = 175
    private val readMoreText = "...Read more"
    private val readMoreTextColor = "#FFFFFF"
    private val limitWithReadMore = limit + readMoreText.length

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
                    binding.loadingInfo.isInvisible = true
                    response.data?.let {
                        tvdbSuccess(it)
                    }
                }

                is Resource.Error -> {
                    binding.loadingInfo.isInvisible = true
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
                    binding.loadingInfo.isInvisible = true
                    response.data?.let {
                        tmdbSuccess(it)
                    }
                }

                is Resource.Error -> {
                    binding.loadingInfo.isInvisible = true
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
//        val materialTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
//            duration = 150L
//        }
//        TransitionManager.beginDelayedTransition(binding.root, materialTransition)

        val pairsArray: MutableList<Pairs> = mutableListOf()

        it.vote_average?.let {
            pairsArray.add(Pairs("Rating", it.toString()))
        }

        it.release_date?.let {
            pairsArray.add(Pairs("Released", it))
        }

        it.runtime?.let {
            pairsArray.add(Pairs("Runtime", "$it min"))
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
                Pairs("Companies", companies.joinToString(separator = ", "))
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
                if (plot.length > limit) {
                    val stringBuilder = SpannableStringBuilder()

                    val plotText = SpannableString(plot.substring(0, limit))
                    plotText.setSpan(
                        ForegroundColorSpan(Color.parseColor("#DEFFFFFF")),
                        0, limit, 0
                    )

                    stringBuilder.append(plotText)

                    val readMore = SpannableString(readMoreText)
                    readMore.setSpan(
                        ForegroundColorSpan(Color.parseColor(readMoreTextColor)),
                        0, readMoreText.length, 0
                    )
                    readMore.setSpan(StyleSpan(Typeface.BOLD), 0, readMoreText.length, 0)
                    stringBuilder.append(readMore)

                    setText(stringBuilder, TextView.BufferType.SPANNABLE)
                    setOnClickListener {
                        TransitionManager.beginDelayedTransition(binding.root)
                        if (text.length > limitWithReadMore) {
                            setText(stringBuilder, TextView.BufferType.SPANNABLE)
                        } else {
                            text = plot
                        }
                    }
                } else {
                    text = plot
                    setOnClickListener(null)
                }
            }
        }
    }


    private fun tvdbSuccess(seriesResponse: SeriesResponse) {
//        val materialTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
//            duration = 150L
//        }
//        TransitionManager.beginDelayedTransition(binding.root, materialTransition)

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
                    if (plot.length > limit) {
                        val stringBuilder = SpannableStringBuilder()

                        val plotText = SpannableString(plot.substring(0, limit))
                        plotText.setSpan(
                            ForegroundColorSpan(Color.parseColor("#DEFFFFFF")),
                            0, limit, 0
                        )

                        stringBuilder.append(plotText)

                        val readMore = SpannableString(readMoreText)
                        readMore.setSpan(
                            ForegroundColorSpan(Color.parseColor(readMoreTextColor)),
                            0, readMoreText.length, 0
                        )
                        readMore.setSpan(StyleSpan(Typeface.BOLD), 0, readMoreText.length, 0)
                        stringBuilder.append(readMore)

                        setText(stringBuilder, TextView.BufferType.SPANNABLE)
                        setOnClickListener {
                            TransitionManager.beginDelayedTransition(binding.root)
                            if (text.length > limitWithReadMore) {
                                setText(stringBuilder, TextView.BufferType.SPANNABLE)
                            } else {
                                text = plot
                            }
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
            loadingInfo.isVisible = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

