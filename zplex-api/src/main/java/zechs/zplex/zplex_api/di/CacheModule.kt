package zechs.zplex.zplex_api.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import zechs.zplex.zplex_api.data.local.cache.CatalogCacheDao
import zechs.zplex.zplex_api.data.local.cache.CatalogCacheDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    @Provides
    @Singleton
    fun provideCatalogCacheDatabase(
        @ApplicationContext context: Context
    ): CatalogCacheDatabase = Room.databaseBuilder(
        context,
        CatalogCacheDatabase::class.java,
        CatalogCacheDatabase.NAME
    ).fallbackToDestructiveMigration(dropAllTables = true).build()

    @Provides
    @Singleton
    fun provideCatalogCacheDao(database: CatalogCacheDatabase): CatalogCacheDao =
        database.catalogCacheDao()
}
