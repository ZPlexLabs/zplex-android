package zechs.zplex.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isInvisible
import androidx.core.widget.doAfterTextChanged
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
import zechs.zplex.databinding.FragmentLoginBinding
import zechs.zplex.utils.MaterialMotionInterpolator
import zechs.zplex.utils.ext.navigateSafe

class LoginFragment : Fragment() {

    companion object {
        const val TAG = "LoginFragment"
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<LoginViewModel>()

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
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.inputUsername.doAfterTextChanged { viewModel.onUsernameChanged(it.toString()) }
        binding.inputPassword.doAfterTextChanged { viewModel.onPasswordChanged(it.toString()) }

        binding.btnSignup.setOnClickListener {
            findNavController().navigateSafe(R.id.action_loginFragment_to_signupFragment)
        }

        binding.inputPassword.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onLoginClicked()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.btnLogin.setOnClickListener {
            viewModel.onLoginClicked()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.btnLogin.isInvisible = state.isLoading
                    binding.buttonProgress.isInvisible = !state.isLoading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is LoginEvent.ShowError -> {
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

                        LoginEvent.LoginSuccess -> {
                            findNavController().navigateSafe(R.id.action_loginFragment_to_landingFragment)
                        }

                        LoginEvent.LoginSuccessButNoCapability -> {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.dialog_title_account_incomplete)
                                .setMessage(R.string.dialog_message_no_capability)
                                .setPositiveButton(R.string.dialog_button_got_it, null)
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