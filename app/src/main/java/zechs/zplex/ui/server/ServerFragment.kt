package zechs.zplex.ui.server

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.FragmentServerBinding
import zechs.zplex.utils.MaterialMotionInterpolator

class ServerFragment : Fragment() {

    companion object {
        const val TAG = "ServerFragment"
    }

    private var _binding: FragmentServerBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<ServerViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeInputFields()
        setupInputActions()
    }

    private fun observeInputFields() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.inputHost.apply {
                        val current = text?.toString().orEmpty()
                        if (current != state.host) {
                            setText(state.host)
                            setSelection(state.host.length)
                        }
                    }

                    binding.inputPort.apply {
                        val current = text?.toString().orEmpty()
                        if (current != state.port) {
                            setText(state.port)
                            setSelection(state.port.length)
                        }
                    }

                    binding.inputHost.isEnabled = !state.isConnecting
                    binding.inputPort.isEnabled = !state.isConnecting

                    if (state.isConnecting != binding.buttonProgress.isVisible) {
                        TransitionManager.beginDelayedTransition(
                            binding.root,
                            MaterialSharedAxis(MaterialSharedAxis.X, state.isConnecting)
                                .apply {
                                    interpolator = if (state.isConnecting) {
                                        MaterialMotionInterpolator.getEmphasizedInterpolator()
                                    } else MaterialMotionInterpolator.getEmphasizedDecelerateInterpolator()
                                }
                        )
                    }
                    if (state.isConnecting) {
                        binding.btnConnect.isInvisible = true
                        binding.buttonProgress.isInvisible = false
                    } else {
                        binding.btnConnect.isInvisible = false
                        binding.buttonProgress.isInvisible = true
                    }

                    binding.btnCancel.isEnabled = state.isConnecting
                }
            }
        }
    }

    private fun setupInputActions() {
        binding.inputPort.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onConnectClicked()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.inputHost.doAfterTextChanged {
            viewModel.onHostChanged(it.toString())
        }
        binding.inputPort.doAfterTextChanged {
            viewModel.onPortChanged(it.toString())
        }
        binding.btnConnect.setOnClickListener {
            viewModel.onConnectClicked()
        }
        binding.btnCancel.setOnClickListener {
            viewModel.onCancelClicked()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is ServerEvent.ShowError -> {
                            val message = if (event.args.isNotEmpty()) {
                                getString(event.messageRes, *event.args.toTypedArray())
                            } else {
                                getString(event.messageRes)
                            }
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.failed_to_connect))
                                .setMessage(message)
                                .setPositiveButton(R.string.ok) { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
                        }

                        is ServerEvent.ConnectionSuccessful -> {
                            Snackbar.make(
                                binding.root,
                                "Connection successful!",
                                Snackbar.LENGTH_SHORT
                            ).show()
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