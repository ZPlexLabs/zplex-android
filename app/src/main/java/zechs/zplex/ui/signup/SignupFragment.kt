package zechs.zplex.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.FragmentSignupBinding
import zechs.zplex.utils.MaterialMotionInterpolator
import zechs.zplex.utils.ext.navigateSafe

class SignupFragment : Fragment() {

    companion object {
        const val TAG = "SignupFragment"
    }

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<SignupViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            interpolator = MaterialMotionInterpolator.getEmphasizedDecelerateInterpolator()
            duration = 300L
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            interpolator = MaterialMotionInterpolator.getEmphasizedAccelerateInterpolator()
            duration = 200L
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.inputFirstName.doOnTextChanged { text, _, _, _ -> viewModel.onFirstNameChanged(text.toString()) }
        binding.inputLastName.doOnTextChanged { text, _, _, _ -> viewModel.onLastNameChanged(text.toString()) }
        binding.inputUsername.doOnTextChanged { text, _, _, _ -> viewModel.onUsernameChanged(text.toString()) }
        binding.inputPassword.doOnTextChanged { text, _, _, _ -> viewModel.onPasswordChanged(text.toString()) }

        binding.btnSignup.setOnClickListener {
            viewModel.onSignupClicked()
        }

        binding.btnLogin.setOnClickListener {
            findNavController().navigateSafe(R.id.action_signupFragment_to_loginFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.buttonProgress.isVisible = state.isLoading
                    binding.btnSignup.isEnabled = !state.isLoading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is SignupEvent.SignupSuccessful -> {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.signup_success))
                                .setMessage(getString(R.string.signup_success_message))
                                .setPositiveButton(android.R.string.ok) { _, _ ->
                                    findNavController().navigateSafe(R.id.action_signupFragment_to_loginFragment)
                                }
                                .show()
                        }

                        is SignupEvent.ShowError -> {
                            val message = if (event.args.isNotEmpty()) {
                                getString(event.messageRes, *event.args.toTypedArray())
                            } else {
                                getString(event.messageRes)
                            }
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.login_failed))
                                .setMessage(message)
                                .setPositiveButton(android.R.string.ok, null)
                                .show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}