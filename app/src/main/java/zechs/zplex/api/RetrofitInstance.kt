package zechs.zplex.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import zechs.zplex.api.interfaces.DriveAPI
import zechs.zplex.api.interfaces.TmdbAPI
import zechs.zplex.api.interfaces.WitchAPI
import zechs.zplex.utils.Constants.GOOGLE_API_URL
import zechs.zplex.utils.Constants.TMDB_API_URL
import zechs.zplex.utils.Constants.WITCH_API_URL

class RetrofitInstance {

    companion object {
        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        private val logging = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        private val drive_api by lazy {
            Retrofit.Builder()
                .baseUrl(GOOGLE_API_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
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

        private val tmdb_api by lazy {
            Retrofit.Builder()
                .baseUrl(TMDB_API_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .build()
                )
                .build()
        }

        val api_tmdb: TmdbAPI by lazy {
            tmdb_api.create(TmdbAPI::class.java)
        }

        private val witch_api by lazy {
            Retrofit.Builder()
                .baseUrl(WITCH_API_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
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