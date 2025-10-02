package zechs.zplex.data.repository

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import zechs.zplex.R
import zechs.zplex.data.model.ErrorResponse
import zechs.zplex.data.model.LoginRequest
import zechs.zplex.data.model.LoginSuccessResponse
import zechs.zplex.data.model.SignupRequest
import zechs.zplex.data.remote.ZPlexApi
import zechs.zplex.utils.state.Result
import java.io.IOException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: ZPlexApi,
    private val moshi: Moshi,
    @param:ApplicationContext private val context: Context
) {

    suspend fun login(username: String, password: String): Result<LoginSuccessResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.login(LoginRequest(username, password))

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.Success(body)
                    } else {
                        Result.Error(
                            message = context.getString(R.string.login_failed),
                            details = context.getString(R.string.login_empty_details)
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val adapter = moshi.adapter(ErrorResponse::class.java)
                    val errorResponse: ErrorResponse? = errorBody
                        ?.takeIf { it.isNotBlank() }
                        ?.let { adapter.fromJson(it) }

                    val userMessage = when (response.code()) {
                        401 -> context.getString(R.string.login_incorrect_credentials)
                        403 -> context.getString(R.string.login_not_authorized)
                        404 -> context.getString(R.string.login_resource_not_found)
                        500 -> context.getString(R.string.login_server_unavailable)
                        else -> errorResponse?.message ?: context.getString(R.string.login_failed)
                    }

                    Result.Error(
                        message = userMessage,
                        details = errorResponse?.details
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Result.Error(
                    message = context.getString(R.string.login_no_connection),
                    details = e.message
                )
            } catch (e: HttpException) {
                e.printStackTrace()
                Result.Error(
                    message = context.getString(R.string.login_http_error, e.code()),
                    details = e.message()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Result.Error(
                    message = context.getString(R.string.login_unexpected_error),
                    details = e.message
                )
            }
        }
    }

    suspend fun signup(
        firstName: String,
        lastName: String,
        username: String,
        password: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.signup(SignupRequest(firstName, lastName, username, password))
                val body = response.body()?.toString() ?: response.errorBody()?.string()
                Log.d("Auth", body ?: "blank body")
                when {
                    response.isSuccessful -> Result.Success(Unit)
                    response.code() == 409 -> Result.Error(
                        message = context.getString(R.string.signup_duplicate_user),
                        details = context.getString(R.string.signup_duplicate_user_details)
                    )

                    response.code() == 400 || response.code() >= 500 -> {
                        val adapter = moshi.adapter(ErrorResponse::class.java)
                        val errorResponse: ErrorResponse? = body
                            ?.takeIf { it.isNotBlank() }
                            ?.let { adapter.fromJson(it) }

                        Result.Error(
                            message = errorResponse?.message
                                ?: context.getString(R.string.something_went_wrong),
                            details = errorResponse?.details
                                ?: context.getString(R.string.something_went_wrong_details)
                        )
                    }

                    else -> {
                        Result.Error(
                            message = context.getString(R.string.something_went_wrong),
                            details = context.getString(R.string.something_went_wrong_details)
                        )
                    }
                }

            } catch (e: IOException) {
                Result.Error(
                    message = context.getString(R.string.error_network),
                    details = e.localizedMessage
                        ?: context.getString(R.string.error_network_details)
                )
            } catch (e: Exception) {
                Result.Error(
                    message = context.getString(R.string.something_went_wrong),
                    details = e.localizedMessage
                        ?: context.getString(R.string.something_went_wrong_details)
                )
            }
        }
    }

}
