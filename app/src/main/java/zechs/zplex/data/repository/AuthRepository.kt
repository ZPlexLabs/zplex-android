package zechs.zplex.data.repository

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import zechs.zplex.R
import zechs.zplex.data.model.LoginErrorResponse
import zechs.zplex.data.model.LoginRequest
import zechs.zplex.data.model.LoginSuccessResponse
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
                    val adapter = moshi.adapter(LoginErrorResponse::class.java)
                    val errorResponse: LoginErrorResponse? = errorBody
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
}
