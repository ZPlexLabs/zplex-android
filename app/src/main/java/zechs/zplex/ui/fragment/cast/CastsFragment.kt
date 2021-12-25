package zechs.zplex.ui.fragment.cast

import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.adapter.media.AboutDataModel
import zechs.zplex.adapter.media.adapters.CurationAdapter
import zechs.zplex.databinding.FragmentCastDetailsBinding
import zechs.zplex.models.tmdb.ProfileSize
import zechs.zplex.models.tmdb.credit.CastObject
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.image.BigImageViewModel
import zechs.zplex.ui.fragment.viewmodels.CastDetailsViewModel
import zechs.zplex.ui.fragment.viewmodels.ShowViewModel
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp
import zechs.zplex.utils.Resource

class CastsFragment : Fragment(R.layout.fragment_cast_details) {

    private var _binding: FragmentCastDetailsBinding? = null
    private val binding get() = _binding!!

    private val castArgsViewModel by activityViewModels<CastDetailsViewModel>()
    private val showsViewModel by activityViewModels<ShowViewModel>()
    private val bigImageViewModel: BigImageViewModel by activityViewModels()
    private lateinit var castViewModel: CastViewModel

    private val knowForAdapter by lazy { CurationAdapter() }

    private val thisTAG = "CastsFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, true
        ).apply {
            duration = 500L
        }

        exitTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, false
        ).apply {
            duration = 500L
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCastDetailsBinding.bind(view)

        castViewModel = (activity as ZPlexActivity).castViewModel
        setupRecyclerView()

        castArgsViewModel.castArgs.observe(viewLifecycleOwner, { castArgs ->
            castViewModel.getCredit(castArgs.personId, castArgs.creditId)
        })

        castViewModel.cast.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { doOnMediaSuccess(it) }
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        val errorMsg = if (message.isEmpty()) {
                            resources.getString(R.string.something_went_wrong)
                        } else message
                        Log.e(thisTAG, errorMsg)
                        binding.apply {
                            pbDetails.isGone = true
                            successView.isGone = true
                            errorView.root.isVisible = true
                        }
                        binding.errorView.apply {
                            errorTxt.text = errorMsg
                        }
                    }
                }
                is Resource.Loading -> {
                    binding.apply {
                        chipGroupActorMeta.removeAllViews()
                        pbDetails.isVisible = true
                        successView.isGone = true
                        binding.errorView.root.isGone = true
                    }
                }
            }
        })
    }

    private fun doOnMediaSuccess(cast: CastObject) {
        val profileUrl = if (cast.profile_path != null) {
            "${TMDB_IMAGE_PREFIX}/${ProfileSize.h632}${cast.profile_path}"
        } else {
            R.drawable.no_actor
        }

        context?.let { c ->
            GlideApp.with(c)
                .load(profileUrl)
                .placeholder(R.drawable.no_actor)
                .into(binding.actorImage)

            cast.job?.let {
                addChip(it, c, R.drawable.ic_work_24)
            }

            cast.birthday?.let {
                addChip("Born in ${it.take(4)}", c, R.drawable.ic_child_24)
            }

            cast.deathday?.let {
                addChip("Died in ${it.take(4)}", c, R.drawable.ic_face_sad_24)
            }
            val genderIcon = when (cast.gender) {
                0 -> R.drawable.ic_transgender_24dp
                1 -> R.drawable.ic_female_24dp
                2 -> R.drawable.ic_male_24dp
                else -> R.drawable.ic_transgender_24dp
            }

            addChip(cast.genderName, c, genderIcon)

            cast.place_of_birth?.let {
                addChip(it, c, R.drawable.ic_place_24)
            }
        }
        binding.apply {
            actorName.text = cast.name
            tvBiography.text = if (cast.biography.isNullOrEmpty()) {
                "No biography available"
            } else cast.biography
            tvBiography.setOnClickListener {
                TransitionManager.beginDelayedTransition(binding.root)
                tvBiography.maxLines = if (tvBiography.lineCount > 4) 4 else 1000
            }
            actorImage.setOnClickListener {
                bigImageViewModel.setImagePath(cast.profile_path)
                findNavController().navigate(R.id.action_castsFragment_to_bigImageFragment)
            }
        }

        val knownForList = cast.known_for.map {
            AboutDataModel.Curation(
                id = it.id,
                media_type = it.media_type,
                name = it.name,
                poster_path = it.poster_path,
                title = it.title,
                vote_average = it.vote_average
            )
        }

        val hideKnown = knownForList.isEmpty()
        binding.apply {
            textView2.isGone = hideKnown
            rvKnowFor.isGone = hideKnown
        }
        knowForAdapter.differ.submitList(knownForList)

        binding.apply {
            pbDetails.isGone = true
            successView.isVisible = true
            binding.errorView.root.isGone = true
        }
        TransitionManager.beginDelayedTransition(binding.root)
    }

    private fun addChip(name: String, context: Context, drawable: Int) {
        val mChip = layoutInflater.inflate(
            R.layout.item_person_meta,
            binding.root,
            false
        ) as Chip

        mChip.text = name
        mChip.chipIcon = ContextCompat.getDrawable(context, drawable)
        binding.chipGroupActorMeta.addView(mChip)
    }

    private fun setupRecyclerView() {
        binding.rvKnowFor.apply {
            adapter = knowForAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.HORIZONTAL, false
            )
            itemAnimator = null
        }
        knowForAdapter.setOnItemClickListener {
            if (it is AboutDataModel.Curation) {
                val media = Media(
                    id = it.id,
                    media_type = it.media_type,
                    name = it.name,
                    poster_path = it.poster_path,
                    title = it.title,
                    vote_average = it.vote_average
                )
                if (it.media_type != null) {
                    showsViewModel.setMedia(it.id, it.media_type, media)
                    findNavController().navigate(R.id.action_castsFragment_to_fragmentMedia)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            rvKnowFor.adapter = null
        }
        _binding = null
    }
}