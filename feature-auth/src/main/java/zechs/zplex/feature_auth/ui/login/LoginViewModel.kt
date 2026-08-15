package zechs.zplex.feature_auth.ui.login

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
import zechs.zplex.common.utils.Result
import zechs.zplex.feature_auth.R
import zechs.zplex.feature_auth.data.repository.AuthRepository
import zechs.zplex.feature_auth.data.repository.ConfigRepository
import zechs.zplex.zplex_api.data.local.accounts.SavedAccount
import zechs.zplex.zplex_api.data.local.accounts.SavedAccountsStore
import zechs.zplex.zplex_api.data.local.config.ConfigStorage
import zechs.zplex.zplex_api.data.local.session.SessionStorage
import zechs.zplex.zplex_api.data.local.user.UserStorage
import zechs.zplex.zplex_api.data.remote.api.config.model.Capability
import zechs.zplex.zplex_api.data.remote.api.config.model.ConfigResponse
import zechs.zplex.zplex_api.utils.JwtUtil
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val configRepository: ConfigRepository,
    private val sessionStorage: SessionStorage,
    private val userStorage: UserStorage,
    private val configStorage: ConfigStorage,
    private val savedAccountsStore: SavedAccountsStore
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
                            userStorage.saveUser(payload)
                            sessionStorage.saveAccessToken(login.accessToken)
                            sessionStorage.saveRefreshToken(login.refreshToken)
                            savedAccountsStore.upsert(
                                SavedAccount(
                                    username = payload.username,
                                    firstName = payload.firstName,
                                    lastName = payload.lastName,
                                    accessToken = login.accessToken,
                                    refreshToken = login.refreshToken,
                                    capabilities = payload.capabilities,
                                    isAdult = payload.isAdult,
                                    tokenType = login.tokenType
                                )
                            )
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
                    configStorage.saveConfig(configResponse.data)
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
                    configStorage.saveCapabilities(capabilities.data)
                }
            }
        }
    }

}