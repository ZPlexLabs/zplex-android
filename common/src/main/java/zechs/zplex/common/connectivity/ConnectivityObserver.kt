package zechs.zplex.common.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

enum class ConnectivityStatus { Available, Unavailable }

interface ConnectivityObserver {
    fun observe(): Flow<ConnectivityStatus>
    fun currentStatus(): ConnectivityStatus
}

@Singleton
class NetworkConnectivityObserver @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<ConnectivityStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(ConnectivityStatus.Available)
            }

            override fun onLost(network: Network) {
                trySend(ConnectivityStatus.Unavailable)
            }

            override fun onUnavailable() {
                trySend(ConnectivityStatus.Unavailable)
            }
        }
        trySend(currentStatus())
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    override fun currentStatus(): ConnectivityStatus {
        val capabilities = connectivityManager.activeNetwork
            ?.let(connectivityManager::getNetworkCapabilities)
        val online = capabilities
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        return if (online) ConnectivityStatus.Available else ConnectivityStatus.Unavailable
    }
}
