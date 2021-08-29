package zechs.zplex.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import zechs.zplex.utils.Constants.Companion.GOOGLE_API_URL
import zechs.zplex.utils.Constants.Companion.WITCH_API_URL

class RetrofitInstance {

    companion object {

        private val drive_api by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            Retrofit.Builder()
                .baseUrl(GOOGLE_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .authenticator(TokenAuthenticator())
                        .build()
                )
                .build()
        }

        val api: DriveAPI by lazy {
            drive_api.create(DriveAPI::class.java)
        }

        private val witch_api by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            Retrofit.Builder()
                .baseUrl(WITCH_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .build()
                )
                .build()
        }

        val api_witch: WitchAPI by lazy {
            witch_api.create(WitchAPI::class.java)
        }

    }
}