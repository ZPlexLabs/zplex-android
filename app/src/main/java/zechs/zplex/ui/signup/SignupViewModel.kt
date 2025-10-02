package zechs.zplex.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.data.repository.AuthRepository
import zechs.zplex.utils.state.Result
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState

    private val _events = Channel<SignupEvent>()
    val events = _events.receiveAsFlow()

    fun onFirstNameChanged(value: String) {
        _uiState.update { it.copy(firstName = value) }
    }

    fun onLastNameChanged(value: String) {
        _uiState.update { it.copy(lastName = value) }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun onSignupClicked() {
        val state = _uiState.value
        if (state.firstName.isBlank() || state.lastName.isBlank() || state.username.isBlank() || state.password.isBlank()) {
            viewModelScope.launch {
                _events.send(SignupEvent.ShowError(R.string.signup_invalid_form_details))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = authRepository.signup(
                firstName = state.firstName,
                lastName = state.lastName,
                username = state.username,
                password = state.password
            )

            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is Result.Success -> _events.send(SignupEvent.SignupSuccessful)
                is Result.Error -> _events.send(
                    SignupEvent.ShowError(
                        R.string.something_went_wrong_reason,
                        listOf(result.details ?: result.message)
                    )
                )
            }
        }
    }
}