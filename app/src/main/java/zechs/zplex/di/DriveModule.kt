package zechs.zplex.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import zechs.zplex.data.remote.DriveApi
import zechs.zplex.data.remote.TokenApi
import zechs.zplex.utils.Constants.GOOGLE_ACCOUNTS_URL
import zechs.zplex.utils.Constants.GOOGLE_API
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DriveModule {

    @Provides
    @Singleton
    fun provideDriveApi(
        @Named("OkHttpClientWithAuthenticator")
        client: OkHttpClient,
        moshi: Moshi
    ): DriveApi {
        return Retrofit.Builder()
            .baseUrl(GOOGLE_API)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(DriveApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenApi(
        @Named("OkHttpClient")
        client: OkHttpClient,
        moshi: Moshi
    ): TokenApi {
        return Retrofit.Builder()
            .baseUrl(GOOGLE_ACCOUNTS_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TokenApi::class.java)
    }
}