package zechs.zplex.zplex_api.data.local.cache

import androidx.room.Entity

@Entity(tableName = "catalog_items", primaryKeys = ["cacheKey", "tmdbId"])
data class CatalogItemEntity(
    val cacheKey: String,
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val imdbRating: Double?,
    val release: String,
    val position: Int,
    val cachedAt: Long
)
