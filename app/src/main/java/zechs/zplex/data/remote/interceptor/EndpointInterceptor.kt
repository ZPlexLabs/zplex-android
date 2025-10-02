package zechs.zplex.data.remote.interceptor

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import zechs.zplex.utils.ApiManager
import java.io.IOException
import java.net.URL
import javax.inject.Inject

class EndpointInterceptor @Inject constructor(
    private val apiManager: ApiManager
) : Interceptor {

    companion object {
        private const val TAG = "EndpointInterceptor"
    }

    @Volatile
    private var cachedHost: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val apiHost = cachedHost ?: runBlocking {
            apiManager.fetchApi().also { cachedHost = it }
        }

        if (!apiHost.isNullOrEmpty()) {
            try {
                val newUrl = URL(apiHost)
                val updatedUrl = request.url.newBuilder()
                    .scheme(newUrl.protocol)
                    .host(newUrl.host)
                    .port(newUrl.port.takeIf { it != -1 } ?: request.url.port)
                    .build()

                val newRequest = request.newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(updatedUrl)
                    .build()

                return chain.proceed(newRequest)

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "Bad URL '$apiHost' -> ${e.message}")
                throw IOException("Invalid API host: $apiHost", e)
            }
        }

        throw IOException("API host not configured")
    }
}
