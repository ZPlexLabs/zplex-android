package zechs.zplex.ui.fragment.cast

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import zechs.zplex.R
import zechs.zplex.adapter.cast.CastDataAdapter
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.ui.BaseFragment
import zechs.zplex.ui.activity.main.MainActivity
import zechs.zplex.ui.fragment.image.BigImageViewModel
import zechs.zplex.utils.Resource
import zechs.zplex.utils.navigateSafe

class CastsFragment : BaseFragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val args: CastsFragmentArgs by navArgs()

    private val bigImageViewModel by activityViewModels<BigImageViewModel>()
    private lateinit var castViewModel: CastViewModel

    private var hasLoaded: Boolean = false

    private val castDataAdapter by lazy {
        CastDataAdapter(
            context = requireContext(),
            setOnClickListener = { navigateMedia(it) },
            expandBiography = {}
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        castViewModel = (activity as MainActivity).castViewModel
        setupRecyclerView()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupCastViewModel(args.cast.id)
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

    private fun navigateMedia(media: Media) {
        val action = CastsFragmentDirections.actionCastsFragmentToFragmentMedia(
            media.copy(media_type = media.media_type ?: "movie")
        )
        findNavController().navigateSafe(action)
    }

    private fun setupCastViewModel(personId: Int) {
        if (!hasLoaded) {
            castViewModel.getPerson(personId)
        }
        castViewModel.personResponse.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is Resource.Success -> response.data?.let {
                        TransitionManager.beginDelayedTransition(
                            binding.root,
                            MaterialFadeThrough()
                        )
                        castDataAdapter.submitList(it)
                        isLoading(false)
                        hasLoaded = true
                    }
                    is Resource.Error -> {
                        showSnackBar(response.message)
                        binding.rvList.isInvisible = true
                    }
                    is Resource.Loading -> if (!hasLoaded) {
                        isLoading(true)
                    }
                }
            }
        }
    }

    private fun isLoading(hide: Boolean) {
        binding.apply {
            loading.isInvisible = !hide
            rvList.isInvisible = hide
        }
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = castDataAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
        }
    }

    private fun showSnackBar(message: String?) {
        Snackbar.make(
            binding.root,
            message ?: resources.getString(R.string.something_went_wrong),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvList.adapter = null
        _binding = null
    }

    companion object {
        const val TAG = "CastsFragment"
    }

}