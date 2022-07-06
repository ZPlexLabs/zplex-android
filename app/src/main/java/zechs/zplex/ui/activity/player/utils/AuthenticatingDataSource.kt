package zechs.zplex.ui.activity.player.utils

import android.net.Uri
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Log
import zechs.zplex.ui.activity.player.PlayerActivity.Companion.TAG
import zechs.zplex.utils.SessionManager
import java.io.IOException


class AuthenticatingDataSource(
    private val wrappedDataSource: DefaultHttpDataSource,
    private val sessionManager: SessionManager,
) : DataSource {

    class Factory(
        private val wrappedFactory: DefaultHttpDataSource.Factory,
        private val sessionManager: SessionManager
    ) : DataSource.Factory {
        override fun createDataSource(): AuthenticatingDataSource {
            return AuthenticatingDataSource(
                wrappedFactory.createDataSource(),
                sessionManager
            )
        }
    }

    private var upstreamOpened = false

    override fun addTransferListener(transferListener: TransferListener) {
        Assertions.checkNotNull(transferListener)
        wrappedDataSource.addTransferListener(transferListener)
    }

    @Throws(IOException::class)
    override fun open(dataSpec: DataSpec): Long {
        upstreamOpened = true
        return try {
            wrappedDataSource.open(dataSpec)
        } catch (e: HttpDataSource.InvalidResponseCodeException) {
            if (e.responseCode == 401) {
                // Token expired, trying to refresh it
                val accessToken = sessionManager.fetchAuthToken()
                wrappedDataSource.setRequestProperty("Authorization", "Bearer $accessToken")
                // we should refresh but we are not refreshing yet
                Log.d(TAG, "Token expired, refreshing...")
                Log.d(TAG, "ACCESS_TOKEN=$accessToken")
            }
            if (e.responseCode == 403) {
                // Unauthorized
                val accessToken = sessionManager.fetchAuthToken()
                wrappedDataSource.setRequestProperty("Authorization", "Bearer $accessToken")
                Log.d(TAG, "Unauthorized, attaching token...")
                Log.d(TAG, "ACCESS_TOKEN=$accessToken")
            }
            wrappedDataSource.open(dataSpec)
        }
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        return wrappedDataSource.read(buffer, offset, readLength)
    }

    override fun getUri(): Uri? {
        return wrappedDataSource.uri
    }

    override fun getResponseHeaders(): Map<String, List<String>> {
        return wrappedDataSource.responseHeaders
    }

    @Throws(IOException::class)
    override fun close() {
        if (upstreamOpened) {
            upstreamOpened = false
            wrappedDataSource.close()
        }
    }
}