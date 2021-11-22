package zechs.zplex.models.drive

import androidx.annotation.Keep

@Keep
data class TokenRequest(
    var client_id: String,
    var client_secret: String,
    var refresh_token: String,
    var grant_type: String = "refresh_token"
)