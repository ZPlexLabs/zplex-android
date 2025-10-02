package zechs.zplex.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.data.repository.AuthRepository
import zechs.zplex.utils.state.Result
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onLoginClicked() {
        val username = _uiState.value.username.trim()
        val password = _uiState.value.password

        if (username.isEmpty() || password.isEmpty()) {
            viewModelScope.launch {
                _events.send(LoginEvent.ShowError(R.string.login_failed))
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // TODO: store in datastore
            val result = authRepository.login(username, password)
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is Result.Success -> _events.send(LoginEvent.LoginSuccess(result.data))
                is Result.Error -> _events.send(
                    LoginEvent.ShowError(
                        R.string.something_went_wrong_reason,
                        listOf(result.details ?: result.message)
                    )
                )
            }
        }
    }
}