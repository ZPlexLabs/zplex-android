package zechs.zplex.zplex_api.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import zechs.zplex.zplex_api.data.local.downloads.DownloadDao
import zechs.zplex.zplex_api.data.local.downloads.DownloadsDatabase
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadsModule {

    @Provides
    @Singleton
    fun provideDownloadsDatabase(
        @ApplicationContext context: Context
    ): DownloadsDatabase = Room.databaseBuilder(
        context,
        DownloadsDatabase::class.java,
        DownloadsDatabase.NAME
    ).fallbackToDestructiveMigration(dropAllTables = true).build()

    @Provides
    @Singleton
    fun provideDownloadDao(database: DownloadsDatabase): DownloadDao = database.downloadDao()

    @Provides
    @Singleton
    @Named("download_client")
    fun provideDownloadClient(
        @Named("base_client") baseHttpClient: OkHttpClient
    ): OkHttpClient = baseHttpClient.newBuilder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(0, TimeUnit.SECONDS)
        .build()
}
