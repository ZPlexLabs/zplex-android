package zechs.zplex.di

import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import zechs.zplex.data.local.WatchedMovieDao
import zechs.zplex.data.local.WatchedShowDao
import zechs.zplex.data.repository.WatchedRepository
import zechs.zplex.utils.SessionManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Singleton
    @Provides
    fun provideSessionDataStore(
        @ApplicationContext appContext: Context,
        gson: Gson
    ): SessionManager = SessionManager(appContext, gson)

    @Singleton
    @Provides
    fun provideWatchedRepository(
        watchedShowDao: WatchedShowDao,
        watchedMovieDao: WatchedMovieDao
    ) = WatchedRepository(watchedShowDao, watchedMovieDao)

}