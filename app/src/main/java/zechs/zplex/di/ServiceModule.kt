package zechs.zplex.di

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import zechs.zplex.data.remote.RemoteLibrary
import zechs.zplex.data.repository.DriveRepository
import zechs.zplex.data.repository.RemoteLibraryRepository
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.service.RemoteLibraryIndexingService
import zechs.zplex.utils.BuildNotificationUtils
import zechs.zplex.utils.SessionManager

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        return BuildNotificationUtils.buildIndexingServiceNotification(
            context,
            RemoteLibraryIndexingService.INDEXING_SERVICE_NOTIFICATION_CHANNEL_ID
        )
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @ServiceScoped
    @Provides
    fun provideRemoteLibrary(
        notificationManager: NotificationManager,
        driveRepository: DriveRepository,
        tmdbRepository: TmdbRepository,
        sessionManager: SessionManager,
        @ApplicationContext context: Context
    ): RemoteLibrary {
        return RemoteLibraryRepository(notificationManager, driveRepository, tmdbRepository, sessionManager, context)
    }

}