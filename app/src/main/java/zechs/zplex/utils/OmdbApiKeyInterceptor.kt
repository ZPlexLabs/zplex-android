package zechs.zplex.utils

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import zechs.zplex.utils.Constants.OMDB_API_URL

class OmdbApiKeyInterceptor(
    private val apiKey: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        return if (url.startsWith(OMDB_API_URL)) {
            val newRequestUrl = addApiKeyIfNotPresent(request)
            val newRequest = request.newBuilder().url(newRequestUrl).build()
            return chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }

    private fun addApiKeyIfNotPresent(request: Request): HttpUrl {
        val url = request.url
        val hasApiKey = url.queryParameter("apikey") != null
        return if (hasApiKey) {
            url
        } else {
            url.newBuilder().addQueryParameter("apikey", apiKey).build()
        }
    }

}