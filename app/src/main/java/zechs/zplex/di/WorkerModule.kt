package zechs.zplex.di

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import zechs.zplex.data.local.offline.OfflineEpisodeDao
import zechs.zplex.data.local.offline.OfflineSeasonDao
import zechs.zplex.data.local.offline.OfflineShowDao
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.service.CombinedWorkerFactory
import zechs.zplex.service.DownloadWorkerFactory
import zechs.zplex.service.OfflineDatabaseWorkerFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Singleton
    @Provides
    fun provideWorkManager(
        @ApplicationContext context: Context,
        combinedWorkerFactory: CombinedWorkerFactory
    ): WorkManager {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(combinedWorkerFactory)
            .build()
        WorkManager.initialize(context, config)
        return WorkManager.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideOfflineDatabaseWorkerFactory(
        offlineShowDao: OfflineShowDao,
        offlineSeasonDao: OfflineSeasonDao,
        offlineEpisodeDao: OfflineEpisodeDao,
        tmdbRepository: TmdbRepository
    ): OfflineDatabaseWorkerFactory {
        return OfflineDatabaseWorkerFactory(
            offlineShowDao, offlineSeasonDao, offlineEpisodeDao,
            tmdbRepository
        )
    }

    @Singleton
    @Provides
    fun provideCombinedWorkerFactory(
        downloadWorkerFactory: DownloadWorkerFactory,
        offlineDatabaseWorkerFactory: OfflineDatabaseWorkerFactory
    ): CombinedWorkerFactory {
        return CombinedWorkerFactory(downloadWorkerFactory, offlineDatabaseWorkerFactory)
    }
}
