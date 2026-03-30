package zechs.zplex.googledrive.data.model

import android.net.Uri
import androidx.annotation.Keep
import androidx.core.net.toUri
import zechs.zplex.googledrive.config.DriveConfig
import java.io.Serializable

data class DriveClient(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scopes: List<String>
) : Serializable {

    fun authUrl(): Uri? = try {
        ("${DriveConfig.GOOGLE_ACCOUNTS_URL}/o/oauth2/auth?" +
                "response_type=code&approval_prompt=force&access_type=offline" +
                "&client_id=${clientId}&redirect_uri=${redirectUri}&scope=${scopes.joinToString(" ")}").toUri()
    } catch (e: Exception) {
        null
    }
}