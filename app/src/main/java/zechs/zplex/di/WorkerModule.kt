package zechs.zplex.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import zechs.zplex.zplex_api.data.download.MediaDownloadWorkerFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Singleton
    @Provides
    fun provideWorkManager(
        @ApplicationContext context: Context,
        mediaDownloadWorkerFactory: MediaDownloadWorkerFactory
    ): WorkManager {
        val config = Configuration.Builder()
            .setWorkerFactory(mediaDownloadWorkerFactory)
            .build()
        WorkManager.initialize(context, config)
        return WorkManager.getInstance(context)
    }
}
