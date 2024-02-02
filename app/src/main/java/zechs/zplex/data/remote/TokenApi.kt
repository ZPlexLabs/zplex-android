package zechs.zplex.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import zechs.zplex.data.model.drive.AuthorizationResponse
import zechs.zplex.data.model.drive.AuthorizationTokenRequest
import zechs.zplex.data.model.drive.RefreshTokenRequest
import zechs.zplex.data.model.drive.TokenResponse

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