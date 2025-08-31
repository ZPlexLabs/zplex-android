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
import zechs.zplex.data.local.api_cache.ApiCacheDao
import zechs.zplex.data.local.offline.OfflineEpisodeDao
import zechs.zplex.data.local.offline.OfflineSeasonDao
import zechs.zplex.data.local.offline.OfflineShowDao
import zechs.zplex.data.repository.DriveRepository
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.service.CacheCleanupWorkerFactory
import zechs.zplex.service.DelegatingWorkerFactory
import zechs.zplex.service.DownloadWorkerFactory
import zechs.zplex.service.OfflineDatabaseWorkerFactory
import zechs.zplex.utils.SessionManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Singleton
    @Provides
    fun provideWorkManager(
        @ApplicationContext context: Context,
        delegatingWorkerFactory: DelegatingWorkerFactory
    ): WorkManager {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(delegatingWorkerFactory)
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
    fun provideDownloadWorkerFactory(
        driveRepository: DriveRepository,
        sessionManager: SessionManager
    ): DownloadWorkerFactory {
        return DownloadWorkerFactory(driveRepository, sessionManager)
    }

    @Singleton
    @Provides
    fun provideDelegatingWorkerFactory(
        downloadWorkerFactory: DownloadWorkerFactory,
        offlineDatabaseWorkerFactory: OfflineDatabaseWorkerFactory,
        cacheCleanupWorkerFactory: CacheCleanupWorkerFactory
    ): DelegatingWorkerFactory {
        return DelegatingWorkerFactory(
            downloadWorkerFactory,
            offlineDatabaseWorkerFactory,
            cacheCleanupWorkerFactory
        )
    }

    @Singleton
    @Provides
    fun provideCacheCleanupWorkerFactory(
        apiCacheDao: ApiCacheDao
    ): CacheCleanupWorkerFactory {
        return CacheCleanupWorkerFactory(apiCacheDao)
    }
}
