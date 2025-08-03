package zechs.zplex.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import zechs.zplex.data.remote.OmdbApi
import zechs.zplex.utils.Constants.OMDB_API_URL
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OmdbModule {

    @Provides
    @Singleton
    fun provideOmdbApi(
        @Named("OmdbHttpClient")
        client: OkHttpClient,
        moshi: Moshi
    ): OmdbApi {
        return Retrofit.Builder()
            .baseUrl(OMDB_API_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OmdbApi::class.java)
    }

}