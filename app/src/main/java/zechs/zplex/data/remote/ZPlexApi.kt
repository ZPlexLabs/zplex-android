package zechs.zplex.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import zechs.zplex.data.model.LoginRequest
import zechs.zplex.data.model.LoginSuccessResponse
import zechs.zplex.data.model.SignupRequest
import zechs.zplex.data.model.config.Capability
import zechs.zplex.data.model.config.ConfigResponse
import zechs.zplex.utils.NoAuth

interface ZPlexApi {

    @POST("/api/auth/login")
    @NoAuth
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginSuccessResponse>

    @POST("/api/auth/signup")
    @NoAuth
    suspend fun signup(
        @Body signupRequest:
        SignupRequest
    ): Response<Unit>

    @GET("/api/config")
    suspend fun config(): Response<ConfigResponse>

    @GET("/api/config/capabilities")
    suspend fun capabilities(): Response<List<Capability>>
}