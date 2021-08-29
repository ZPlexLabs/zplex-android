package zechs.zplex.models.drive

import com.google.gson.annotations.SerializedName

data class TokenRequest(
    @SerializedName("client_id")
    var client_id: String,

    @SerializedName("client_secret")
    var client_secret: String,

    @SerializedName("refresh_token")
    var refresh_token: String,

    @SerializedName("grant_type")
    var grant_type: String = "refresh_token"
)