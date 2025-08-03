package zechs.zplex.data.model.drive

import android.net.Uri
import androidx.annotation.Keep
import zechs.zplex.utils.Constants.GOOGLE_ACCOUNTS_URL
import java.io.Serializable
import androidx.core.net.toUri

@Keep
data class DriveClient(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scopes: List<String>
) : Serializable {

    fun authUrl(): Uri? = try {
        ("${GOOGLE_ACCOUNTS_URL}/o/oauth2/auth?" +
                "response_type=code&approval_prompt=force&access_type=offline" +
                "&client_id=${clientId}&redirect_uri=${redirectUri}&scope=${scopes.joinToString(" ")}").toUri()
    } catch (e: Exception) {
        null
    }
}
