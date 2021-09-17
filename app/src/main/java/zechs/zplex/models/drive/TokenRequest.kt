package zechs.zplex.models.drive

import com.squareup.moshi.Json

data class TokenRequest(
    @Json(name = "client_id")
    var client_id: String,

    @Json(name = "client_secret")
    var client_secret: String,

    @Json(name = "refresh_token")
    var refresh_token: String,

    @Json(name = "grant_type")
    var grant_type: String = "refresh_token"
)