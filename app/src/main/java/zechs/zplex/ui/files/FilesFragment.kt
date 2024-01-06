package zechs.zplex.ui.files

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.AbsListView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import zechs.zplex.R
import zechs.zplex.data.model.drive.DriveFile
import zechs.zplex.databinding.FragmentFilesBinding
import zechs.zplex.ui.files.adapter.FilesAdapter
import zechs.zplex.ui.files.adapter.FilesDataModel
import zechs.zplex.ui.folder_picker.FolderPickerActivity
import zechs.zplex.utils.FolderPickerResultContract
import zechs.zplex.utils.FolderType
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.util.DriveApiQueryBuilder


@AndroidEntryPoint
class FilesFragment : Fragment() {

    companion object {
        const val TAG = "FilesFragment"
    }

    private var _binding: FragmentFilesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by lazy {
        ViewModelProvider(this)[FilesViewModel::class.java]
    }

    private val args by navArgs<FilesFragmentArgs>()

    private var isLoading = false
    private var isScrolling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(
            /* axis */ MaterialSharedAxis.X,
            /* forward */ true
        ).apply {
            interpolator = LinearInterpolator()
            duration = 300
        }

        returnTransition = MaterialSharedAxis(
            /* axis */ MaterialSharedAxis.X,
            /* forward */ false
        ).apply {
            interpolator = LinearInterpolator()
            duration = 300
        }

        exitTransition = MaterialSharedAxis(
            /* axis */ MaterialSharedAxis.X,
            /* forward */ true
        ).apply {
            interpolator = LinearInterpolator()
            duration = 250
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilesBinding.inflate(
            inflater, container, /* attachToParent */false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFilesBinding.bind(view)

        // Workaround for transition animation
        // https://github.com/material-components/material-components-android/issues/1984
        val colorBackground = MaterialColors.getColor(view, android.R.attr.colorBackground)
        view.setBackgroundColor(colorBackground)

        binding.toolbar.apply {
            title = args.name
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        Log.d(TAG, "FilesFragment(name=${args.name}, query=${args.query})")

        setupRecyclerView()
        setupFilesObserver()

        binding.selectContainer.isGone = args.id == null
        binding.btnSelect.setOnClickListener {
            handleFolderSelection()
        }
    }

    private fun handleFolderSelection() {
        val data = Intent().also {
            it.putExtra("id", args.id)
            it.putExtra("name", args.name)
        }
        val type = requireActivity().intent!!.getStringExtra(FolderPickerActivity.EXTRA_TYPE)!!
        when (FolderType.valueOf(type)) {
            FolderType.MOVIES -> requireActivity().setResult(
                FolderPickerResultContract.RESULT_MOVIE_FOLDER,
                data
            )

            FolderType.SHOWS -> requireActivity().setResult(
                FolderPickerResultContract.RESULT_SHOW_FOLDER,
                data
            )
        }
        requireActivity().finish()
    }

    private fun setupFilesObserver() {
        if (!viewModel.hasLoaded) {
            viewModel.queryFiles(args.query)
        }

        viewModel.filesList.observe(viewLifecycleOwner) { response ->
            handleFilesList(response)
        }
    }

    private fun handleFilesList(response: Resource<List<FilesDataModel>>) {
        when (response) {
            is Resource.Success -> response.data?.let { files ->
                onSuccess(files)
            }

            is Resource.Error -> {
                showSnackBar(response.message)
                showError(response.message)
            }

            is Resource.Loading -> {
                isLoading = true
                if (!viewModel.hasLoaded || viewModel.hasFailed) {
                    isLoading(true)
                }
                binding.error.root.apply {
                    if (isVisible) {
                        isGone = true
                    }
                }
            }
        }
    }

    private fun onSuccess(files: List<FilesDataModel>) {
        Log.d(TAG, "onSuccess(files=${files.size})")
        if (!viewModel.hasLoaded) {
            doTransition(MaterialFadeThrough())
        }

        isLoading(false)
        isLoading = false
        viewModel.hasLoaded = true

        if (files.isEmpty()) {
            binding.error.apply {
                root.isVisible = true
                errorTxt.text = getString(R.string.no_files_found)
            }
        } else {
            binding.error.root.apply {
                if (isVisible) {
                    isGone = true
                }
            }
        }

        filesAdapter.submitList(files.toMutableList())
    }

    private fun doTransition(transition: Transition) {
        TransitionManager.beginDelayedTransition(
            binding.root, transition
        )
    }

    private fun isLoading(hide: Boolean) {
        binding.apply {
            loading.isInvisible = !hide
            rvList.isInvisible = hide
        }
    }

    private fun showError(msg: String?) {
        binding.apply {
            rvList.isInvisible = true
            loading.isInvisible = true
            error.apply {
                root.isVisible = true
                errorTxt.text = msg ?: getString(R.string.something_went_wrong)
                retryBtn.apply {
                    isVisible = true
                    setOnClickListener {
                        viewModel.queryFiles(args.query)
                    }
                }
            }
        }
        isLoading = false
    }

    private val filesAdapter by lazy {
        FilesAdapter(onClickListener = { handleFileOnClick(it) })
    }

    private fun handleFileOnClick(file: DriveFile) {
        Log.d(TAG, file.toString())
        if (file.isFolder && !file.isShortcut) {
            val action = FilesFragmentDirections.actionFilesFragmentSelf(
                id = file.id,
                name = file.name,
                query = DriveApiQueryBuilder()
                    .inParents(file.id)
                    .trashed(false)
                    .build()
            )
            findNavController().navigate(action)
        } else if (file.isShortcut && file.isShortcutFolder) {
            val action = FilesFragmentDirections.actionFilesFragmentSelf(
                id = file.shortcutDetails.targetId!!,
                name = file.name,
                query = DriveApiQueryBuilder()
                    .inParents(file.shortcutDetails.targetId)
                    .trashed(false)
                    .build()
            )
            findNavController().navigate(action)
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isLastPage = viewModel.isLastPage

            if (isAtLastItem && !isLoading && !isLastPage && isScrolling) {
                Log.d(TAG, "Paginating...")
                viewModel.queryFiles(args.query)
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setupRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(
            /* context */ context,
            /* orientation */ RecyclerView.VERTICAL,
            /* reverseLayout */ false
        )
        binding.rvList.apply {
            adapter = filesAdapter
            layoutManager = linearLayoutManager
            addItemDecoration(
                DividerItemDecoration(context, linearLayoutManager.orientation)
            )
            addOnScrollListener(this@FilesFragment.scrollListener)
        }
    }

    private fun showSnackBar(message: String?) {
        val snackBar = Snackbar.make(
            binding.root,
            message ?: getString(R.string.something_went_wrong),
            Snackbar.LENGTH_SHORT
        )
        val snackBarView = snackBar.view
        val textView = snackBarView.findViewById<View>(
            com.google.android.material.R.id.snackbar_text
        ) as TextView
        textView.maxLines = 5
        snackBar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.rvList.adapter = null
        _binding = null
    }

}