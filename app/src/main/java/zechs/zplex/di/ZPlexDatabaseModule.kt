package zechs.zplex.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import zechs.zplex.data.local.MovieDao
import zechs.zplex.data.local.ShowDao
import zechs.zplex.data.local.WatchedMovieDao
import zechs.zplex.data.local.WatchedShowDao
import zechs.zplex.data.local.WatchlistDatabase
import zechs.zplex.utils.MIGRATION_1_2
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ZPlexDatabaseModule {

    private const val DATABASE_NAME = "zplex_db.db"

    @Singleton
    @Provides
    fun provideZplexDatabase(
        @ApplicationContext appContext: Context
    ) = Room.databaseBuilder(
        appContext,
        WatchlistDatabase::class.java,
        DATABASE_NAME
    )
        .addMigrations(MIGRATION_1_2)
        .build()

    @Singleton
    @Provides
    fun provideMovieDao(
        db: WatchlistDatabase
    ): MovieDao {
        return db.getMovieDao()
    }

    @Singleton
    @Provides
    fun provideShowDao(
        db: WatchlistDatabase
    ): ShowDao {
        return db.getShowDao()
    }

    @Singleton
    @Provides
    fun provideWatchedMovieDao(
        db: WatchlistDatabase
    ): WatchedMovieDao {
        return db.getWatchedMovieDao()
    }

    @Singleton
    @Provides
    fun provideWatchedShowDao(
        db: WatchlistDatabase
    ): WatchedShowDao {
        return db.getWatchedShowDao()
    }

}