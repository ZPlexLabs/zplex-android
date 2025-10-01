package zechs.zplex.ui.server

data class ServerUiState(
    val host: String = "",
    val port: String = "",
    val isConnecting: Boolean = false
)