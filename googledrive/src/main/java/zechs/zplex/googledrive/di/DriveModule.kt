package zechs.zplex.googledrive.di

import com.squareup.moshi.Moshi
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import zechs.zplex.googledrive.config.DriveConfig
import zechs.zplex.googledrive.data.local.DriveClientStore
import zechs.zplex.googledrive.data.remote.api.drive.DriveApi
import zechs.zplex.googledrive.data.remote.api.token.TokenApi
import zechs.zplex.googledrive.data.remote.auth.TokenAuthenticator
import zechs.zplex.googledrive.data.repository.DriveRepository
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DriveModule {

    @Provides
    @Singleton
    @Named("google_drive_http_client")
    fun provideOkHttpClientWithAuthenticator(
        @Named("base_client") baseClientBuilder: OkHttpClient.Builder,
        @Named("google_drive_authenticator") tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return baseClientBuilder
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideDriveApi(
        @Named("google_drive_http_client")
        client: OkHttpClient,
        moshi: Moshi
    ): DriveApi {
        return Retrofit.Builder()
            .baseUrl(DriveConfig.GOOGLE_API)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(DriveApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenApi(
        @Named("base_client") baseClientBuilder: OkHttpClient.Builder,
        moshi: Moshi
    ): TokenApi {
        return Retrofit.Builder()
            .baseUrl(DriveConfig.GOOGLE_ACCOUNTS_URL)
            .client(baseClientBuilder.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TokenApi::class.java)
    }


    @Provides
    @Singleton
    @Named("google_drive_authenticator")
    fun provideTokenAuthenticator(
        driveRepository: Lazy<DriveRepository>,
        driveClientStore: Lazy<DriveClientStore>
    ): TokenAuthenticator {
        return TokenAuthenticator(driveRepository, driveClientStore)
    }

}