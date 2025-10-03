package zechs.zplex.ui.main

import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import zechs.zplex.utils.UserSession
import java.nio.charset.Charset
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userSession: UserSession
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _hasLoggedIn = MutableStateFlow(false)
    val hasLoggedIn: StateFlow<Boolean> = _hasLoggedIn.asStateFlow()

    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            Log.d(TAG, "Checking user session...")
            val isLoggedIn = isUserSessionValid()
            Log.d(TAG, "User session valid: $isLoggedIn")
            _hasLoggedIn.value = isLoggedIn
        }
    }

    private suspend fun isUserSessionValid(): Boolean {
        val user = userSession.fetchUserSession()
        val accessToken = userSession.fetchAccessToken()
        val refreshToken = userSession.fetchRefreshToken()

        if (user == null || accessToken.isNullOrEmpty() || refreshToken.isNullOrEmpty()) {
            Log.d(TAG, "One or more session values are missing. User not logged in.")
            return false
        }

        val refreshTokenValid = isTokenValid(refreshToken)
        Log.d(TAG, "Refresh token valid: $refreshTokenValid")

        return refreshTokenValid
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e(TAG, "Token format invalid: does not have 3 parts")
                return false
            }

            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val payloadJson = JSONObject(String(decodedBytes, Charset.forName("UTF-8")))

            val exp = payloadJson.optLong("exp", 0L)
            val now = Date().time / 1000

            Log.d(TAG, "Token expiry: $exp, current time: $now")

            now < exp
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode token", e)
            false
        }
    }
}
