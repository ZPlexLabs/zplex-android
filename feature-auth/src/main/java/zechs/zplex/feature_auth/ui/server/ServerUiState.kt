package zechs.zplex.feature_auth.ui.server

data class ServerUiState(
    val host: String = "",
    val port: String = "",
    val isConnecting: Boolean = false
)