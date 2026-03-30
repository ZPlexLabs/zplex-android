package zechs.zplex.common.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import zechs.zplex.common.BuildConfig
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 🔹 Logging
    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.HEADERS)
    }

    @Provides
    @Singleton
    @Named("base_client")
    fun provideMainOkHttp(
        logging: HttpLoggingInterceptor,
        dns: Dns,
    ): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .dns(dns)
            .also {
                if (BuildConfig.DEBUG) it.addInterceptor(logging)
            }
    }



}