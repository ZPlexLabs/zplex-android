package zechs.zplex.ui.drive_menu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.databinding.FragmentDriveMenuBinding
import zechs.zplex.ui.folder_picker.FolderPickerActivity
import zechs.zplex.utils.ext.navigateSafe
import zechs.zplex.utils.util.DriveApiQueryBuilder

class DriveMenuFragment : Fragment() {

    companion object {
        const val TAG = "DriveMenuFragment"
    }

    private var _binding: FragmentDriveMenuBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentDriveMenuBinding.inflate(
            inflater, container, /* attachToParent */false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDriveMenuBinding.bind(view)

        binding.apply {
            requireActivity().intent?.getStringExtra(FolderPickerActivity.EXTRA_TITLE)
                ?.let { toolbar.title = it }

            // My drive
            navigateToFiles(
                button = btnMyDrive,
                name = getString(R.string.my_drive),
                query = DriveApiQueryBuilder()
                    .inParents("root")
                    .trashed(false)
                    .build()
            )

            // Shared drives
            navigateToFiles(
                button = btnSharedDrives,
                name = getString(R.string.shared_drives),
                query = null
            )

            // Shared with me
            navigateToFiles(
                button = btnSharedWithMe,
                name = getString(R.string.shared_with_me),
                query = "sharedWithMe=true"
            )

            // Starred
            navigateToFiles(
                button = btnStarred,
                name = getString(R.string.starred),
                query = "starred=true"
            )

        }

    }

    private fun <T : MaterialButton> navigateToFiles(
        button: T, name: String, query: String?
    ) {
        button.setOnClickListener {
            val action = DriveMenuFragmentDirections.actionDriveMenuFragmentToFilesFragment(
                id = null,
                name = name,
                query = query,
            )
            findNavController().navigateSafe(action)
            Log.d(TAG, "navigateToFiles(name=$name, query=$query)")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}