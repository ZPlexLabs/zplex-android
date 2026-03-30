package zechs.zplex.googledrive.data.remote.auth

import android.util.Log
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import zechs.zplex.common.utils.Resource
import zechs.zplex.googledrive.data.local.DriveClientStore
import zechs.zplex.googledrive.data.repository.DriveRepository
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val driveRepository: Lazy<DriveRepository>,
    private val driveClientStore: Lazy<DriveClientStore>
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
    }

    override fun authenticate(
        route: Route?, response: Response
    ): Request? {

        val client = runBlocking {
            driveClientStore.get().fetch()
        } ?: return null

        val tokenResponse = runBlocking {
            driveRepository.get().fetchAccessToken(client, forceRefresh = true)
        }

        if (tokenResponse is Resource.Success) {
            tokenResponse.data?.let { token ->
                val newAccessToken = token.accessToken
                Log.d(TAG, "Received new access token (token=$newAccessToken)")
                return response.request.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $newAccessToken")
                    .url(response.request.url.toString())
                    .build()
            }
        } else {
            Log.d(TAG, tokenResponse.message!!)
        }

        return null
    }

}