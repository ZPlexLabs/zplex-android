package zechs.zplex.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zechs.zplex.R
import zechs.zplex.data.model.config.Capability
import zechs.zplex.data.model.config.ConfigResponse
import zechs.zplex.data.repository.AuthRepository
import zechs.zplex.data.repository.ConfigRepository
import zechs.zplex.utils.JwtUtil
import zechs.zplex.utils.UserSession
import zechs.zplex.utils.state.Result
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val configRepository: ConfigRepository,
    private val userSession: UserSession
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

        viewModelScope.launch(Dispatchers.IO) {
            val result = authRepository.login(username, password)
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is Result.Success -> {
                    try {
                        val login = result.data
                        val payload = JwtUtil.decodeJwtPayload(login.accessToken)
                        if (payload?.capabilities.isNullOrEmpty()) {
                            _events.send(LoginEvent.LoginSuccessButNoCapability)
                        } else {
                            userSession.saveUserSession(payload)
                            userSession.saveAccessToken(login.accessToken)
                            userSession.saveRefreshToken(login.refreshToken)
                            val asyncConfig = async { saveConfig() }
                            val asyncCapabilities = async { saveCapabilities() }
                            asyncConfig.await()
                            asyncCapabilities.await()
                            _events.send(LoginEvent.LoginSuccess)
                        }
                    } catch (e: Exception) {
                        _events.send(
                            LoginEvent.ShowError(
                                R.string.something_went_wrong_reason,
                                listOf("${e::class.simpleName}: ${e.message}")
                            )
                        )
                    }
                }

                is Result.Error -> _events.send(
                    LoginEvent.ShowError(
                        R.string.something_went_wrong_reason,
                        listOf(result.details ?: result.message)
                    )
                )
            }
        }
    }

    private suspend fun saveConfig() {
        withContext(Dispatchers.IO) {
            val configResponse = configRepository.config()
            when (configResponse) {
                is Result.Error -> {
                    _events.send(
                        LoginEvent.ShowError(
                            R.string.something_went_wrong_reason,
                            listOf(configResponse.details ?: configResponse.message)
                        )
                    )
                }

                is Result.Success<ConfigResponse> -> {
                    userSession.saveConfig(configResponse.data)
                }
            }
        }
    }

    private suspend fun saveCapabilities() {
        withContext(Dispatchers.IO) {
            val capabilities = configRepository.capabilities()
            when (capabilities) {
                is Result.Error -> {
                    _events.send(
                        LoginEvent.ShowError(
                            R.string.something_went_wrong_reason,
                            listOf(capabilities.details ?: capabilities.message)
                        )
                    )
                }

                is Result.Success<List<Capability>> -> {
                    userSession.saveCapabilities(capabilities.data)
                }
            }
        }
    }

}