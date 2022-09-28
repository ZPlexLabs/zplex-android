package zechs.zplex.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import zechs.zplex.data.local.MovieDao
import zechs.zplex.data.local.ShowDao
import zechs.zplex.data.remote.TmdbApi
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.utils.Constants.TMDB_API_URL
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TmdbModule {

    @Provides
    @Singleton
    fun provideTmdbApi(
        client: OkHttpClient,
        moshi: Moshi
    ): TmdbApi {
        return Retrofit.Builder()
            .baseUrl(TMDB_API_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TmdbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTmdbRepository(
        tmdbApi: TmdbApi,
        movieDao: MovieDao,
        showDao: ShowDao
    ): TmdbRepository {
        return TmdbRepository(tmdbApi, movieDao, showDao)
    }

}