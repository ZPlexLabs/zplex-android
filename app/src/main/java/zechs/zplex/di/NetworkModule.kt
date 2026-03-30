package zechs.zplex.di

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
import zechs.zplex.utils.Constants.OMDB_API_KEY
import zechs.zplex.utils.OmdbApiKeyInterceptor
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("OmdbApiKeyInterceptor")
    fun provideOmdbApiKeyInterceptor(): Interceptor {
        return OmdbApiKeyInterceptor(OMDB_API_KEY)
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

}