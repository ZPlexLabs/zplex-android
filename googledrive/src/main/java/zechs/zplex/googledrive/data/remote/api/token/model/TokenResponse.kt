package zechs.zplex.googledrive.data.remote.api.token.model

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class TokenResponse(
    @param:Json(name = "access_token")
    val accessToken: String,
    @param:Json(name = "expires_in")
    val expiresIn: Long,
    @param:Json(name = "token_type")
    val tokenType: String,
    val scope: String
)

@Keep
data class AuthorizationResponse(
    @param:Json(name = "access_token")
    val accessToken: String,
    @param:Json(name = "expires_in")
    val expiresIn: Long,
    @param:Json(name = "refresh_token")
    val refreshToken: String,
    @param:Json(name = "token_type")
    val tokenType: String,
    val scope: String
) {

    fun toTokenResponse() = TokenResponse(
        accessToken, expiresIn, tokenType, scope
    )

}