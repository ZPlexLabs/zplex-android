package zechs.zplex.api

import android.net.Uri
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
import zechs.zplex.utils.Constants.Companion.CLIENT_ID
import zechs.zplex.utils.Constants.Companion.CLIENT_SECRET
import zechs.zplex.utils.Constants.Companion.REFRESH_TOKEN
import zechs.zplex.utils.SessionManager
import java.io.IOException


class TokenAuthenticator : Authenticator {

    private val appContext = ThisApp.context
    private val sessionManager = appContext?.let { SessionManager(it) }

    @Throws(IOException::class)
    override fun authenticate(route: Route?, response: Response): Request? {

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val service = Retrofit.Builder()
            .baseUrl(Constants.GOOGLE_OAUTH_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val tokenAPI = service.create(TokenAPI::class.java)

        val call = tokenAPI.getAccessToken(
            TokenRequest(
                CLIENT_ID,
                CLIENT_SECRET,
                REFRESH_TOKEN
            )
        )
        val tokenResponse = call.execute()
        if (tokenResponse.isSuccessful) {
            tokenResponse.body()?.let { res ->
                val newAccessToken = res.access_token
                sessionManager?.saveAuthToken(newAccessToken)
                val currentRequestUrl: Uri = Uri.parse(response.request.url.toString())
                val newRequestUrl =
                    addNewTokenInQuery(currentRequestUrl, newAccessToken)
                println("newRequestUrl:  $newRequestUrl")
                return response.request.newBuilder()
                    .url(newRequestUrl.toString())
                    .build()
            }
        }
        return null
    }

    private fun addNewTokenInQuery(uri: Uri, newValue: String): Uri? {
        val params = uri.queryParameterNames
        val newUri = uri.buildUpon().clearQuery()
        for (param in params) {
            newUri.appendQueryParameter(
                param,
                if (param == "access_token") newValue else uri.getQueryParameter(param)
            )
        }
        return newUri.build()
    }

}