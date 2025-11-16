package zechs.zplex.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import zechs.zplex.BuildConfig
import zechs.zplex.data.repository.DriveRepository
import zechs.zplex.data.repository.TokenAuthenticator
import zechs.zplex.utils.Constants.OMDB_API_KEY
import zechs.zplex.utils.OmdbApiKeyInterceptor
import zechs.zplex.utils.SessionManager
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BASIC)
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        driveRepository: Lazy<DriveRepository>,
        sessionManager: Lazy<SessionManager>
    ): TokenAuthenticator {
        return TokenAuthenticator(driveRepository, sessionManager)
    }

    @Provides
    @Singleton
    @Named("OmdbApiKeyInterceptor")
    fun provideOmdbApiKeyInterceptor(): Interceptor {
        return OmdbApiKeyInterceptor(OMDB_API_KEY)
    }

    @Provides
    @Singleton
    @Named("OkHttpClientWithAuthenticator")
    fun provideOkHttpClientWithAuthenticator(
        logging: Lazy<HttpLoggingInterceptor>,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .also {
                if (BuildConfig.DEBUG) {
                    // Logging only in debug builds
                    it.addInterceptor(logging.get())
                }
                it.authenticator(tokenAuthenticator)
            }.build()
    }

    @Provides
    @Singleton
    @Named("OkHttpClient")
    fun provideOkHttpClient(
        logging: Lazy<HttpLoggingInterceptor>,
        dns: Dns
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .also {
                it.dns(dns)
                if (BuildConfig.DEBUG) {
                    // Logging only in debug builds
                    it.addInterceptor(logging.get())
                }
            }.build()
    }

    @Provides
    @Singleton
    @Named("OmdbHttpClient")
    fun provideOmdbHttpClient(
        logging: Lazy<HttpLoggingInterceptor>,
        @Named("OmdbApiKeyInterceptor") omdbApiKeyInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .also {
                if (BuildConfig.DEBUG) {
                    // Logging only in debug builds
                    it.addInterceptor(logging.get())
                }
                it.addInterceptor(omdbApiKeyInterceptor)
            }.build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

}