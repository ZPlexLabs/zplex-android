package zechs.zplex.api

import android.net.Uri
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import zechs.zplex.ThisApp
import zechs.zplex.models.drive.TokenRequest
import zechs.zplex.utils.Constants
import zechs.zplex.utils.Constants.Companion.CLIENT_ID
import zechs.zplex.utils.Constants.Companion.CLIENT_SECRET
import zechs.zplex.utils.Constants.Companion.REFRESH_TOKEN
import java.io.IOException


class TokenAuthenticator : Authenticator {

    private val appContext = ThisApp.context
    private val sessionManager = appContext?.let { SessionManager(it) }

    @Throws(IOException::class)
    override fun authenticate(route: Route?, response: Response): Request? {

        val service = Retrofit.Builder()
            .baseUrl(Constants.GOOGLE_OAUTH_URL)
            .addConverterFactory(GsonConverterFactory.create())
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