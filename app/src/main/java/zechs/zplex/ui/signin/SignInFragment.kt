package zechs.zplex.ui.signin

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.FragmentSignInBinding
import zechs.zplex.ui.code.DialogCode
import zechs.zplex.utils.Constants.GUIDE_TO_MAKE_DRIVE_CLIENT
import zechs.zplex.utils.ext.hideKeyboardWhenOffFocus
import zechs.zplex.utils.state.Resource

@AndroidEntryPoint
class SignInFragment : Fragment() {

    companion object {
        const val TAG = "SignInFragment"
    }

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private var _codeDialog: DialogCode? = null
    private val codeDialog get() = _codeDialog!!

    private val viewModel by lazy {
        ViewModelProvider(this)[SignInViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(
            /* axis */ MaterialSharedAxis.Y,
            /* forward */ true
        ).apply {
            interpolator = LinearInterpolator()
            duration = 300
        }

        returnTransition = MaterialSharedAxis(
            /* axis */ MaterialSharedAxis.Y,
            /* forward */ false
        ).apply {
            interpolator = LinearInterpolator()
            duration = 300
        }

        exitTransition = MaterialSharedAxis(
            /* axis */ MaterialSharedAxis.Y,
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
        _binding = FragmentSignInBinding.inflate(
            inflater, container, /* attachToParent */false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignInBinding.bind(view)

        driveClientObserver()

        binding.signInText.setOnClickListener {
            if (!updateClient()) {
                return@setOnClickListener
            }
            Log.d(TAG, "Auth url: ${viewModel.getDriveClient()!!.authUrl()}")

            Intent().setAction(Intent.ACTION_VIEW)
                .setData(viewModel.getDriveClient()!!.authUrl())
                .also { startActivity(it) }
        }


        binding.btnHelp.setOnClickListener {
            Intent().setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse(GUIDE_TO_MAKE_DRIVE_CLIENT))
                .also { startActivity(it) }
        }

        binding.enterCode.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Please note")
                .setMessage(getString(R.string.important_note_message))
                .setPositiveButton("Continue") { dialog, _ ->
                    dialog.dismiss()
                    showCodeDialog()
                }.show()
        }

        binding.clientId.editText!!.hideKeyboardWhenOffFocus()
        binding.clientSecret.editText!!.hideKeyboardWhenOffFocus()
        binding.redirectUri.editText!!.hideKeyboardWhenOffFocus()

        loginObserver()
    }

    private fun loginObserver() {
        viewModel.loginStatus.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    findNavController().navigateUp()
                }

                is Resource.Error -> {
                    isLoading(false)
                    Snackbar.make(
                        binding.root,
                        response.message!!,
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                is Resource.Loading -> {
                    isLoading(true)
                }
            }
        }
    }

    private fun showCodeDialog() {
        if (_codeDialog == null) {
            _codeDialog = DialogCode(
                context = requireContext(),
                onSubmitClickListener = { codeUri ->
                    viewModel.requestRefreshToken(codeUri)
                    codeDialog.dismiss()
                }
            )
        }

        codeDialog.also {
            it.show()
            it.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(MATCH_PARENT, WRAP_CONTENT)
            }
            it.setOnDismissListener {
                _codeDialog = null
            }
        }
    }

    private fun isLoading(loading: Boolean) {
        binding.apply {
            this.loading.isVisible = loading
            layoutConfigure.isVisible = !loading
            enterCode.isVisible = !loading
        }
    }

    private fun updateClient(): Boolean {
        val clientId = binding.clientId.editText!!.text.toString()
        val clientSecret = binding.clientSecret.editText!!.text.toString()
        val redirectUri = binding.redirectUri.editText!!.text.toString()
        val scopes = binding.scopes.editText!!.text.toString()
        if (clientId.isEmpty()
            || clientSecret.isEmpty()
            || redirectUri.isEmpty()
            || scopes.isEmpty()
        ) {
            Snackbar.make(
                binding.root,
                getString(R.string.fill_all_fields),
                Snackbar.LENGTH_LONG
            ).show()
            return false
        }

        viewModel.setClient(clientId, clientSecret, redirectUri, listOf(scopes))
        return true
    }

    private fun driveClientObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.client.collect { client ->
                    val hasAlreadyLoggedIn = client != null
                    binding.apply {
                        if (hasAlreadyLoggedIn) {
                            clientId.editText!!.setText(client!!.clientId)
                            clientSecret.editText!!.setText(client.clientSecret)
                            redirectUri.editText!!.setText(client.redirectUri)
                        }
                        clientId.isEnabled = !hasAlreadyLoggedIn
                        clientSecret.isEnabled = !hasAlreadyLoggedIn
                        redirectUri.isEnabled = !hasAlreadyLoggedIn
                        signInText.isEnabled = !hasAlreadyLoggedIn
                        enterCode.isEnabled = !hasAlreadyLoggedIn
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}