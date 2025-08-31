package zechs.zplex.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import zechs.zplex.data.local.offline.OfflineDatabase
import zechs.zplex.data.local.offline.OfflineEpisodeDao
import zechs.zplex.data.local.offline.OfflineMovieDao
import zechs.zplex.data.local.offline.OfflineSeasonDao
import zechs.zplex.data.local.offline.OfflineShowDao
import zechs.zplex.data.local.offline.migrations.MIGRATION_1_2
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OfflineDatabaseModule {

    private const val DATABASE_NAME = "offline.db"

    @Singleton
    @Provides
    fun provideOfflineDatabase(
        @ApplicationContext appContext: Context
    ) = Room.databaseBuilder(
        appContext,
        OfflineDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(MIGRATION_1_2).build()

    @Singleton
    @Provides
    fun provideOfflineShowDao(
        db: OfflineDatabase
    ): OfflineShowDao {
        return db.getOfflineShowDao()
    }

    @Singleton
    @Provides
    fun provideOfflineSeasonDao(
        db: OfflineDatabase
    ): OfflineSeasonDao {
        return db.getOfflineSeasonDao()
    }

    @Singleton
    @Provides
    fun provideOfflineEpisodeDao(
        db: OfflineDatabase
    ): OfflineEpisodeDao {
        return db.getOfflineEpisodeDao()
    }

    @Singleton
    @Provides
    fun provideOfflineMovieDao(
        db: OfflineDatabase
    ): OfflineMovieDao {
        return db.getOfflineMovieDao()
    }

}