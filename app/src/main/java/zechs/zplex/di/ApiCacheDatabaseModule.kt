package zechs.zplex.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import zechs.zplex.data.local.api_cache.ApiCacheDao
import zechs.zplex.data.local.api_cache.ApiCacheDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiCacheDatabaseModule {

    private const val DATABASE_NAME = "api_cache.db"

    @Singleton
    @Provides
    fun provideApiCacheDatabase(
        @ApplicationContext appContext: Context
    ) = Room.databaseBuilder(
        appContext,
        ApiCacheDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideApiCacheDao(
        db: ApiCacheDatabase
    ): ApiCacheDao {
        return db.getApiCacheDao()
    }

}