package zechs.zplex.di

import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import zechs.zplex.BuildConfig
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** Shared client for Coil image loading (see ThisApp.newImageLoader()). */
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

}