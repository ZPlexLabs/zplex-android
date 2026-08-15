package zechs.zplex.zplex_api.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import zechs.zplex.zplex_api.data.remote.api.config.ConfigApi
import zechs.zplex.zplex_api.data.remote.api.login.LoginApi
import zechs.zplex.zplex_api.data.remote.api.me.MeApi
import zechs.zplex.zplex_api.data.remote.api.movies.MovieApi
import zechs.zplex.zplex_api.data.remote.api.stream.StreamApi
import zechs.zplex.zplex_api.data.remote.api.token.TokenApi
import zechs.zplex.zplex_api.data.remote.api.tvshows.TvShowApi
import zechs.zplex.zplex_api.data.remote.interceptor.EndpointInterceptor
import zechs.zplex.zplex_api.data.remote.interceptor.TokenInterceptor
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ZPlexApiModule {

    @Provides
    @Singleton
    @Named("zplex_http_client_for_token")
    fun provideZPlexHttpClientForToken(
        @Named("base_client") baseClientBuilder: OkHttpClient.Builder,
        endpointInterceptor: EndpointInterceptor
    ): OkHttpClient {
        return baseClientBuilder
            .addInterceptor(endpointInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("zplex_http_client")
    fun provideZPlexHttpClient(
        @Named("base_client") baseClientBuilder: OkHttpClient.Builder,
        endpointInterceptor: EndpointInterceptor,
        tokenInterceptor: TokenInterceptor
    ): OkHttpClient {
        return baseClientBuilder
            .addInterceptor(endpointInterceptor)
            .addInterceptor(tokenInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("zplex_retrofit")
    fun provideMainRetrofit(
        @Named("zplex_http_client") client: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://localhost")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideTokenApi(
        @Named("zplex_http_client_for_token") client: OkHttpClient,
        moshi: Moshi
    ): TokenApi {
        return Retrofit.Builder()
            .baseUrl("http://localhost")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TokenApi::class.java)
    }

    @Provides
    @Singleton
    fun provideLoginApi(@Named("zplex_retrofit") retrofit: Retrofit): LoginApi {
        return retrofit.create(LoginApi::class.java)
    }

    @Provides
    @Singleton
    fun provideConfigApi(@Named("zplex_retrofit") retrofit: Retrofit): ConfigApi {
        return retrofit.create(ConfigApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMovieApi(@Named("zplex_retrofit") retrofit: Retrofit): MovieApi {
        return retrofit.create(MovieApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTvShowApi(@Named("zplex_retrofit") retrofit: Retrofit): TvShowApi {
        return retrofit.create(TvShowApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStreamApi(@Named("zplex_retrofit") retrofit: Retrofit): StreamApi {
        return retrofit.create(StreamApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMeApi(@Named("zplex_retrofit") retrofit: Retrofit): MeApi {
        return retrofit.create(MeApi::class.java)
    }
}