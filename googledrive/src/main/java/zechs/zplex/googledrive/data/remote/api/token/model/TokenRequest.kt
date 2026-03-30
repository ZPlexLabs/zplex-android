package zechs.zplex.googledrive.data.remote.api.token.model

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class RefreshTokenRequest(
    @param:Json(name = "client_id")
    val clientId: String,
    @param:Json(name = "client_secret")
    val clientSecret: String,
    @param:Json(name = "refresh_token")
    val refreshToken: String,
    @param:Json(name = "grant_type")
    val grantType: String = "refresh_token"
)

@Keep
data class AuthorizationTokenRequest(
    @param:Json(name = "client_id")
    val clientId: String,
    @param:Json(name = "client_secret")
    val clientSecret: String,
    @param:Json(name = "redirect_uri")
    val redirectUri: String,
    @param:Json(name = "code")
    val authCode: String,
    @param:Json(name = "grant_type")
    val grantType: String = "authorization_code"
)


