package zechs.zplex.ui.image

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.transition.MaterialFadeThrough
import zechs.zplex.R
import zechs.zplex.data.model.PosterSize
import zechs.zplex.databinding.FragmentBigImageBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp

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

            GlideApp.with(binding.bigImageView)
                .load(imageUrl)
                .placeholder(R.drawable.no_poster)
                .addListener(glideRequestListener)
                .into(binding.bigImageView)
        }
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private val glideRequestListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            parentFragment?.startPostponedEnterTransition()
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            parentFragment?.startPostponedEnterTransition()
            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}