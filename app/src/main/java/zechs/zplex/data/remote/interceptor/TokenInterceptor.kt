package zechs.zplex.data.remote.interceptor

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import retrofit2.Invocation
import zechs.zplex.data.model.RefreshRequest
import zechs.zplex.data.remote.ZPlexTokenApi
import zechs.zplex.utils.JwtUtil
import zechs.zplex.utils.NoAuth
import zechs.zplex.utils.UserSession
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val userSession: UserSession,
    private val tokenApi: ZPlexTokenApi
) : Interceptor {

    private val mutex = Mutex()

    companion object {
        private const val TAG = "TokenInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestToProceed = runBlocking { buildRequestWithToken(originalRequest) }
        return chain.proceed(requestToProceed)
    }

    private suspend fun buildRequestWithToken(request: Request): Request {
        return if (isNoAuthEndpoint(request)) {
            removeAuthorizationHeader(request)
        } else {
            attachAuthorizationHeader(request)
        }
    }

    private fun isNoAuthEndpoint(request: Request): Boolean {
        return request.tag(Invocation::class.java)
            ?.method()
            ?.getAnnotation(NoAuth::class.java) != null
    }

    private fun removeAuthorizationHeader(request: Request): Request {
        return request.newBuilder()
            .removeHeader("Authorization")
            .build()
    }

    private suspend fun attachAuthorizationHeader(request: Request): Request {
        val accessToken = getValidAccessToken()
        return if (!accessToken.isNullOrEmpty()) {
            request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            request
        }
    }

    private suspend fun getValidAccessToken(): String? {
        return mutex.withLock {
            val currentToken = userSession.fetchAccessToken()

            val isTokenExpired = currentToken?.let { token ->
                try {
                    val parts = token.split(".")
                    if (parts.size != 3) true
                    else {
                        val payload = String(
                            Base64.decode(parts[1], Base64.URL_SAFE)
                        )
                        val exp = JSONObject(payload).optLong("exp", 0L)
                        val now = System.currentTimeMillis() / 1000
                        exp <= now
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    true
                }
            } ?: true

            if (isTokenExpired) {
                val refreshToken = userSession.fetchRefreshToken()
                if (refreshToken.isNullOrEmpty()) {
                    Log.e(TAG, "No refresh token available, cannot refresh")
                    null
                } else {
                    refreshAccessToken(refreshToken)
                }
            } else {
                currentToken
            }
        }
    }

    private suspend fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val response = tokenApi.refresh(RefreshRequest(refreshToken))

            if (!response.isSuccessful) {
                Log.e(TAG, "Token refresh failed with HTTP code: ${response.code()}")
                return null
            }

            val body = response.body() ?: run {
                Log.e(TAG, "Token refresh response body is null")
                return null
            }

            userSession.saveAccessToken(body.accessToken)
            updateUserSession(body.accessToken)

            Log.i(TAG, "Token refreshed successfully")
            body.accessToken
        } catch (e: Exception) {
            Log.e(TAG, "Exception during token refresh", e)
            null
        }
    }

    private suspend fun updateUserSession(accessToken: String) {
        val user = JwtUtil.decodeJwtPayload(accessToken)
        if (user != null) {
            userSession.saveUserSession(user)
            Log.i(TAG, "UserSession updated with new user: $user")
        } else {
            Log.e(TAG, "Failed to decode JWT payload")
        }
    }
}
