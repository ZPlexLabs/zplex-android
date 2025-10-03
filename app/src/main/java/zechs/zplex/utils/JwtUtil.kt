package zechs.zplex.utils

import android.util.Base64
import com.google.gson.Gson
import zechs.zplex.data.model.User

class JwtUtil {

    companion object {

        fun decodeJwtPayload(token: String): User? {
            return try {
                val parts = token.split(".")
                if (parts.size < 2) return null
                val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE))
                Gson().fromJson(payloadJson, User::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    }

}