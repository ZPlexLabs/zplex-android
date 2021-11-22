package zechs.zplex.models.drive

import androidx.annotation.Keep

@Keep
data class TokenResponse(
    val access_token: String,
    val expires_in: Int,
    val scope: String,
    val token_type: String
)