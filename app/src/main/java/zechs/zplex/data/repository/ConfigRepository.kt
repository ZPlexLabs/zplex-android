package zechs.zplex.data.repository

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import retrofit2.Response
import zechs.zplex.R
import zechs.zplex.data.model.ErrorResponse
import zechs.zplex.data.model.config.Capability
import zechs.zplex.data.model.config.ConfigResponse
import zechs.zplex.data.remote.ZPlexApi
import zechs.zplex.utils.state.Result
import java.io.IOException
import javax.inject.Inject

class ConfigRepository @Inject constructor(
    private val api: ZPlexApi,
    private val moshi: Moshi,
    @param:ApplicationContext private val context: Context
) {

    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): Result<T> {
        return try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.Success(body)
                } else {
                    Result.Error(
                        message = context.getString(R.string.no_data_available_at_the_moment),
                        details = context.getString(R.string.response_body_was_null)
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val adapter = moshi.adapter(ErrorResponse::class.java)
                val errorResponse: ErrorResponse? = errorBody
                    ?.takeIf { it.isNotBlank() }
                    ?.let { adapter.fromJson(it) }

                Result.Error(
                    message = "An unexpected error occurred. Please try again.",
                    details = errorResponse?.details ?: "HTTP ${response.code()}"
                )
            }
        } catch (e: IOException) {
            Result.Error(
                message = context.getString(R.string.login_no_connection),
                details = e.message
            )
        } catch (e: HttpException) {
            Result.Error(
                message = context.getString(R.string.login_http_error, e.code()),
                details = e.message()
            )
        } catch (e: Exception) {
            Result.Error(
                message = context.getString(R.string.login_unexpected_error),
                details = e.message
            )
        }
    }

    suspend fun config(): Result<ConfigResponse> = safeApiCall { api.config() }

    suspend fun capabilities(): Result<List<Capability>> = safeApiCall { api.capabilities() }
}
