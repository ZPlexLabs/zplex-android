package zechs.zplex.feature_home.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import zechs.zplex.feature_home.R
import zechs.zplex.feature_home.databinding.BottomSheetHomeMenuBinding
import zechs.zplex.zplex_api.data.local.user.User

class HomeMenuBottomSheet(
    private val user: User,
    private val onProfileClick: () -> Unit,
    private val onLogoutClick: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetHomeMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetHomeMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.fullname.text = getString(R.string.full_name, user.firstName, user.lastName)
        binding.username.text = user.username

        binding.profile.setOnClickListener {
            dismiss()
            onProfileClick()
        }

        binding.logout.setOnClickListener {
            dismiss()
            onLogoutClick()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}