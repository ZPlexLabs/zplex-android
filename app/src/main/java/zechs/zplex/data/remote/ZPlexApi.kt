package zechs.zplex.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import zechs.zplex.data.model.LoginRequest
import zechs.zplex.data.model.LoginSuccessResponse
import zechs.zplex.data.model.SignupRequest

interface ZPlexApi {

    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginSuccessResponse>

    @POST("/api/auth/signup")
    suspend fun signup(
        @Body signupRequest:
        SignupRequest
    ): Response<Unit>

}