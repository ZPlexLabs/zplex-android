package zechs.zplex.ui.collection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFadeThrough
import zechs.zplex.R
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.FragmentListBinding
import zechs.zplex.ui.cast.CastsFragmentDirections
import zechs.zplex.ui.collection.adapter.CollectionDataAdapter
import zechs.zplex.ui.image.BigImageViewModel
import zechs.zplex.utils.ext.navigateSafe
import zechs.zplex.utils.state.Resource

class FragmentCollection : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<FragmentCollectionArgs>()

    private val bigImageViewModel by activityViewModels<BigImageViewModel>()
    private val collectionViewModel by activityViewModels<CollectionViewModel>()

    private var hasLoaded: Boolean = false

    private val collectionDataAdapter by lazy {
        CollectionDataAdapter(
            setOnClickListener = { navigateMedia(it) },
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

        setupRecyclerView()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupCollectionViewModel(args.collectionId)
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
        val action = FragmentCollectionDirections.actionFragmentCollectionToFragmentMedia(
            media.copy(media_type = media.media_type ?: MediaType.movie)
        )
        Log.d(TAG, "navigateMedia, invoked. ($media)")
        findNavController().navigateSafe(action)
    }

    private fun setupCollectionViewModel(collectionId: Int) {
        if (!hasLoaded) {
            collectionViewModel.getCollection(collectionId)
        }
        collectionViewModel.collectionResponse.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is Resource.Success -> response.data?.let {
                        TransitionManager.beginDelayedTransition(
                            binding.root,
                            MaterialFadeThrough()
                        )
                        collectionDataAdapter.submitList(it)
                        isLoading(false)
                        hasLoaded = true
                    }
                    is Resource.Error -> {
                        Log.d(TAG, "Error: ${response.message}")
                        showToast(response.message)
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
            adapter = collectionDataAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(
            context,
            message ?: resources.getString(R.string.something_went_wrong),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvList.adapter = null
        _binding = null
    }

    companion object {
        const val TAG = "CollectionFragment"
    }

}