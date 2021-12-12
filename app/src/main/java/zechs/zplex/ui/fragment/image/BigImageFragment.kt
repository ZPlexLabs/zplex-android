package zechs.zplex.ui.fragment.image

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import zechs.zplex.R
import zechs.zplex.databinding.FragmentBigImageBinding

class BigImageFragment : DialogFragment(R.layout.fragment_big_image) {

    private var _binding: FragmentBigImageBinding? = null
    private val binding get() = _binding!!

    private val bigImageViewModel: BigImageViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBigImageBinding.bind(view)

        bigImageViewModel.imageUrl.observe(viewLifecycleOwner, { imageUri ->
            context?.let {
                Glide.with(it)
                    .load(imageUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.no_actor)
                    .into(binding.bigImageView)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}