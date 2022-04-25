package zechs.zplex.ui.fragment.cast

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialFade
import zechs.zplex.R
import zechs.zplex.adapter.media.AboutDataModel
import zechs.zplex.adapter.media.adapters.CurationAdapter
import zechs.zplex.databinding.FragmentCastDetailsBinding
import zechs.zplex.models.tmdb.ProfileSize
import zechs.zplex.models.tmdb.credit.CastObject
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.ui.BaseFragment
import zechs.zplex.ui.activity.main.MainActivity
import zechs.zplex.ui.fragment.image.BigImageViewModel
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp
import zechs.zplex.utils.Resource

class CastsFragment : BaseFragment() {

    override val enterTransitionListener: Transition.TransitionListener? = null

    private var _binding: FragmentCastDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: CastsFragmentArgs by navArgs()

    private val bigImageViewModel: BigImageViewModel by activityViewModels()
    private lateinit var castViewModel: CastViewModel

    private val knowForAdapter by lazy {
        CurationAdapter().apply { setHasStableIds(true) }
    }

    private val thisTAG = "CastsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCastDetailsBinding.inflate(inflater, container, false)
        binding.actorImage.transitionName = args.castArgs.profile_path
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        castViewModel = (activity as MainActivity).castViewModel
        setupRecyclerView()
        setupCastObserver()

        val castArgs = args.castArgs

        val profileUrl = if (castArgs.profile_path != null) {
            "${TMDB_IMAGE_PREFIX}/${ProfileSize.h632}${castArgs.profile_path}"
        } else {
            R.drawable.no_actor
        }

        binding.apply {
            GlideApp.with(actorImage)
                .load(profileUrl)
                .placeholder(R.drawable.no_actor)
                .into(actorImage)
            actorName.text = castArgs.name
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            actorImage.setOnClickListener {
                openImageFullSize(castArgs.profile_path, binding.actorImage)
            }
        }
        castViewModel.getCredit(castArgs.personId, castArgs.creditId)
    }

    private fun openImageFullSize(posterPath: String?, imageView: ImageView) {
        imageView.transitionName = posterPath
        this.exitTransition = null
        bigImageViewModel.setImagePath(posterPath)

        val action = CastsFragmentDirections.actionCastsFragmentToBigImageFragment()
        val extras = FragmentNavigatorExtras(
            imageView to imageView.transitionName
        )
        findNavController().navigate(action, extras)
        Log.d("navigateToMedia", imageView.transitionName)
    }

    private fun doOnMediaSuccess(cast: CastObject) {

        val knownForList = cast.known_for.map {
            AboutDataModel.Curation(
                id = it.id,
                media_type = it.media_type,
                name = it.name,
                poster_path = it.poster_path,
                title = it.title,
                vote_average = it.vote_average,
                backdrop_path = it.backdrop_path,
                overview = it.overview,
                release_date = it.release_date
            )
        }

        val hideKnown = knownForList.isEmpty()

        context?.let { c ->

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
            tvBiography.text = if (cast.biography.isNullOrEmpty()) {
                "No biography available"
            } else cast.biography
            tvBiography.setOnClickListener {
                TransitionManager.beginDelayedTransition(binding.root, MaterialFade())
                tvBiography.maxLines = if (tvBiography.lineCount > 4) 4 else 1000
            }
            textView2.isGone = hideKnown
            rvKnowFor.isGone = hideKnown
        }

        knowForAdapter.differ.submitList(knownForList)
        isLoading(false)
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
            castOnClickListener(it)
        }
    }

    private fun castOnClickListener(it: AboutDataModel) {
        if (it is AboutDataModel.Curation) {
            val media = Media(
                id = it.id,
                media_type = it.media_type,
                name = it.name,
                poster_path = it.poster_path,
                title = it.title,
                vote_average = it.vote_average,
                backdrop_path = it.backdrop_path,
                overview = it.overview,
                release_date = it.release_date
            )
            if (it.media_type != null) {
                val action = CastsFragmentDirections.actionCastsFragmentToFragmentMedia(
                    media.copy(media_type = it.media_type)
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun isLoading(loading: Boolean) {
        binding.groupUiElements.isInvisible = loading
        binding.pbDetails.isInvisible = !loading
    }


    private fun setupCastObserver() {
        castViewModel.cast.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { doOnMediaSuccess(it) }
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        val errorMsg = message.ifEmpty {
                            resources.getString(R.string.something_went_wrong)
                        }
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
                        isLoading(true)
                    }
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