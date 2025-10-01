package zechs.zplex.ui.server

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zechs.zplex.BuildConfig
import zechs.zplex.R
import zechs.zplex.ui.server.ServerFragment.Companion.TAG
import zechs.zplex.utils.ApiManager
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ServerViewModel @Inject constructor(
    private val apiManager: ApiManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_HOST = "host"
        private const val KEY_PORT = "port"
        private const val CONNECTION_TIMEOUT_IN_MS = 10_000
    }

    private val _uiState = MutableStateFlow(
        ServerUiState(
            host = savedStateHandle[KEY_HOST]
                ?: if (BuildConfig.DEBUG) "https://example.com" else "",
            port = savedStateHandle[KEY_PORT] ?: if (BuildConfig.DEBUG) "8080" else ""
        )
    )
    val uiState: StateFlow<ServerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ServerEvent>()
    val events: SharedFlow<ServerEvent> = _events.asSharedFlow()

    private var connectJob: Job? = null

    fun onHostChanged(newHost: String) {
        _uiState.update { it.copy(host = newHost) }
        savedStateHandle[KEY_HOST] = newHost
    }

    fun onPortChanged(newPort: String) {
        _uiState.update { it.copy(port = newPort) }
        savedStateHandle[KEY_PORT] = newPort
    }

    fun onConnectClicked() {
        connectJob?.cancel()

        connectJob = viewModelScope.launch {
            val hostInput = _uiState.value.host.trim()
            val portInput = _uiState.value.port.trim()

            if (hostInput.isEmpty() || portInput.isEmpty()) {
                _events.emit(ServerEvent.ShowError(R.string.host_and_port_cannot_be_empty))
                return@launch
            }

            val portNumber = portInput.toIntOrNull()
            if (portNumber == null || portNumber !in 1..65535) {
                _events.emit(ServerEvent.ShowError(R.string.invalid_port_range))
                return@launch
            }

            _uiState.update { it.copy(isConnecting = true) }

            try {
                val hostUrl = URL(hostInput)
                val protocol = hostUrl.protocol
                val host = hostUrl.host
                val port = portNumber

                val baseUrl = URL(protocol, host, port, "")
                val healthUrl = URL(baseUrl, "/health")
                Log.d(TAG, "Trying to connect to server: $healthUrl")

                withContext(Dispatchers.IO) {
                    val connection = (healthUrl.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = CONNECTION_TIMEOUT_IN_MS
                        readTimeout = CONNECTION_TIMEOUT_IN_MS
                    }
                    try {
                        val code = connection.responseCode
                        if (code == 200) {
                            apiManager.saveApi(baseUrl.toString())
                            _events.emit(ServerEvent.ConnectionSuccessful)
                        } else {
                            _events.emit(ServerEvent.ShowError(R.string.error_invalid_server))
                        }
                    } finally {
                        connection.disconnect()
                    }
                }
            } catch (e: MalformedURLException) {
                Log.e(TAG, "Invalid host input", e)
                _events.emit(ServerEvent.ShowError(R.string.invalid_host_missing_protocol))
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e::class.simpleName} ${e.message}", e)
                _events.emit(ServerEvent.ShowError(R.string.error_network))
            } catch (_: CancellationException) {
                Log.d(TAG, "Connection cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error: ${e::class.simpleName} ${e.message}", e)
                _events.emit(
                    ServerEvent.ShowError(
                        R.string.something_went_wrong_reason,
                        listOf(e.message ?: e::class.simpleName ?: "UnknownException")
                    )
                )
            } finally {
                _uiState.update { it.copy(isConnecting = false) }
            }
        }
    }

    fun onCancelClicked() {
        Log.d(TAG, if (connectJob == null) "No job to cancel" else "Cancelled connect job")
        connectJob?.cancel()
        _uiState.update { it.copy(isConnecting = false) }
    }

}