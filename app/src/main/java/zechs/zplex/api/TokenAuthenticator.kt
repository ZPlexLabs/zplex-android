package zechs.zplex.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import zechs.zplex.ThisApp
import zechs.zplex.api.interfaces.TokenAPI
import zechs.zplex.models.drive.TokenRequest
import zechs.zplex.utils.Constants
import zechs.zplex.utils.Constants.CLIENT_ID
import zechs.zplex.utils.Constants.CLIENT_SECRET
import zechs.zplex.utils.Constants.REFRESH_TOKEN
import zechs.zplex.utils.SessionManager


class TokenAuthenticator : Authenticator {

    private val appContext = ThisApp.context
    private val sessionManager = appContext?.let { SessionManager(it) }

    override fun authenticate(route: Route?, response: Response): Request? {

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val service = Retrofit.Builder()
            .baseUrl(Constants.GOOGLE_OAUTH_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val tokenAPI = service.create(TokenAPI::class.java)

        val tokenResponse = tokenAPI.getAccessToken(
            TokenRequest(
                CLIENT_ID,
                CLIENT_SECRET,
                REFRESH_TOKEN
            )
        ).execute()

        if (tokenResponse.isSuccessful) {
            tokenResponse.body()?.let { res ->
                val newAccessToken = res.access_token
                sessionManager?.saveAuthToken(newAccessToken)
                return response.request.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $newAccessToken")
                    .url(response.request.url.toString())
                    .build()
            }
        }
        return null
    }
}