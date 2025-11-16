package zechs.zplex.utils.glide

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GlideOkHttpEntryPoint {

    @Named("OkHttpClient")
    fun okHttpClient(): OkHttpClient

}