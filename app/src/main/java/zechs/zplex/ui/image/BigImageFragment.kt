package zechs.zplex.ui.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.android.material.transition.MaterialFadeThrough
import zechs.zplex.R
import zechs.zplex.data.model.PosterSize
import zechs.zplex.databinding.FragmentBigImageBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX

class BigImageFragment : Fragment() {

    private var _binding: FragmentBigImageBinding? = null
    private val binding get() = _binding!!

    private val bigImageViewModel by activityViewModels<BigImageViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transition = MaterialFadeThrough().apply {
            interpolator = LinearInterpolator()
            duration = 220
        }
        enterTransition = transition.apply { duration = 270 }
        exitTransition = transition
        returnTransition = transition
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBigImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBigImageBinding.bind(view)

        bigImageViewModel.imagePath.observe(viewLifecycleOwner) { imageUri ->
            binding.bigImageView.transitionName = imageUri
            val imageUrl = if (imageUri != null) {
                "${TMDB_IMAGE_PREFIX}/${PosterSize.original}${imageUri}"
            } else R.drawable.no_poster

            binding.bigImageView.load(imageUrl) {
                placeholder(R.drawable.no_poster)
                listener(imageRequestListener)
            }
        }
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private val imageRequestListener = object : ImageRequest.Listener {
        override fun onError(request: ImageRequest, result: ErrorResult) {
            super.onError(request, result)
            parentFragment?.startPostponedEnterTransition()
        }

        override fun onSuccess(request: ImageRequest, result: SuccessResult) {
            super.onSuccess(request, result)
            parentFragment?.startPostponedEnterTransition()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}