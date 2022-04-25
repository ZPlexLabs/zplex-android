package zechs.zplex.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import zechs.zplex.api.interfaces.TmdbAPI
import zechs.zplex.api.interfaces.ZPlexAPI
import zechs.zplex.utils.Constants.TMDB_API_URL
import zechs.zplex.utils.Constants.ZPLEX_API_URL

class RetrofitInstance {

    companion object {

        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        private val moshiConverterFactory = MoshiConverterFactory.create(moshi)

        private val logging = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        private val loggingClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val tmdbApi: TmdbAPI by lazy {
            Retrofit.Builder()
                .baseUrl(TMDB_API_URL)
                .addConverterFactory(moshiConverterFactory)
                .client(loggingClient)
                .build().create(TmdbAPI::class.java)
        }

        val zplexApi: ZPlexAPI by lazy {
            Retrofit.Builder()
                .baseUrl(ZPLEX_API_URL)
                .addConverterFactory(moshiConverterFactory)
                .client(loggingClient)
                .build().create(ZPlexAPI::class.java)
        }
    }

}
