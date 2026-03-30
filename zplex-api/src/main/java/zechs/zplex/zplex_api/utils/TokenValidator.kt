package zechs.zplex.zplex_api.utils

import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.Date
import javax.inject.Inject

class TokenValidator @Inject constructor() {
    companion object {
        const val TAG = "TokenValidator"
    }

    fun isValid(token: String): Boolean {
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