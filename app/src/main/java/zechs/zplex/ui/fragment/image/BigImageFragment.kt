package zechs.zplex.ui.fragment.image

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.databinding.FragmentBigImageBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX

class BigImageFragment : Fragment(R.layout.fragment_big_image) {

    private var _binding: FragmentBigImageBinding? = null
    private val binding get() = _binding!!

    private val bigImageViewModel: BigImageViewModel by activityViewModels()

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
        _binding = FragmentBigImageBinding.bind(view)

        bigImageViewModel.imagePath.observe(viewLifecycleOwner, { imageUri ->
            val profileUrl = if (imageUri != null) {
                "${TMDB_IMAGE_PREFIX}/original${imageUri}"
            } else {
                R.drawable.no_poster
            }
            context?.let {
                Glide.with(it)
                    .load(profileUrl)
                    .placeholder(R.drawable.no_poster)
                    .into(binding.bigImageView)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}