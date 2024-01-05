package zechs.zplex.ui.setup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.FragmentSetupBinding

class SetupFragment : Fragment() {

    companion object {
        const val TAG = "SetupFragment"
    }

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<SetupViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(
            inflater, container, /* attachToParent */false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSetupBinding.bind(view)

        binding.btnChooseMovies.setOnClickListener {
            launchPicker.launch(
                StartFolderPicker(
                    getString(R.string.choose_movies_folder),
                    FolderType.MOVIES
                )
            )
        }
        binding.btnChooseShows.setOnClickListener {
            launchPicker.launch(
                StartFolderPicker(
                    getString(R.string.choose_shows_folder),
                    FolderType.SHOWS
                )
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hasBothFolders.collect { hasBothFolders ->
                    binding.btnDone.isGone = !hasBothFolders
                }
            }
        }

        binding.btnDone.setOnClickListener {
            // restart activity
            viewLifecycleOwner.lifecycleScope.launch {
                requireActivity().finish()
                delay(250L)
                requireActivity().startActivity(requireActivity().intent)
            }
        }
    }

    private val launchPicker = registerForActivityResult(FolderPickerResultContract()) {
        if (it != null) {
            handleSelectedFolder(it)
        }
    }

    private fun handleSelectedFolder(folder: SelectedFolder) {
        Log.d(TAG, "handleSelectedFolder: $folder")
        when (folder.type) {
            FolderType.MOVIES -> handleMoviesFolder(folder.name, folder.id)
            FolderType.SHOWS -> handleShowsFolder(folder.name, folder.id)
        }
    }

    private fun handleMoviesFolder(name: String, id: String) {
        binding.btnChooseMovies.text = name
        viewModel.saveMoviesFolder(id)
        Log.d(TAG, "handleMoviesFolder: $name, $id")
    }

    private fun handleShowsFolder(name: String, id: String) {
        binding.btnChooseShows.text = name
        viewModel.saveShowsFolder(id)
        Log.d(TAG, "handleShowsFolder: $name, $id")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}