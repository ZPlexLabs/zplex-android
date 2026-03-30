package zechs.zplex.googledrive.data.remote.api.token

import retrofit2.http.Body
import retrofit2.http.POST
import zechs.zplex.googledrive.data.remote.api.token.model.AuthorizationResponse
import zechs.zplex.googledrive.data.remote.api.token.model.AuthorizationTokenRequest
import zechs.zplex.googledrive.data.remote.api.token.model.RefreshTokenRequest
import zechs.zplex.googledrive.data.remote.api.token.model.TokenResponse

interface TokenApi {

    @POST("/o/oauth2/token")
    suspend fun getAccessToken(
        @Body request: RefreshTokenRequest
    ): TokenResponse

    @POST("/o/oauth2/token")
    suspend fun getRefreshToken(
        @Body request: AuthorizationTokenRequest
    ): AuthorizationResponse

}