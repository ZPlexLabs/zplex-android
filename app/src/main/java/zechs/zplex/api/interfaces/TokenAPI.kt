package zechs.zplex.api.interfaces

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import zechs.zplex.models.drive.TokenRequest
import zechs.zplex.models.drive.TokenResponse

interface TokenAPI {

    @POST("/o/oauth2/token")
    fun getAccessToken(@Body request: TokenRequest): Call<TokenResponse>

}