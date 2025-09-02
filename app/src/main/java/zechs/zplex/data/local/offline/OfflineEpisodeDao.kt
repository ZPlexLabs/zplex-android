package zechs.zplex.data.local.offline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import zechs.zplex.data.model.offline.OfflineEpisode

@Dao
interface OfflineEpisodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEpisode(offlineEpisode: OfflineEpisode): Long

    @Query("SELECT * FROM episodes WHERE tmdbId = :tmdbId AND seasonNumber = :seasonNumber")
    suspend fun getAllEpisodes(tmdbId: Int, seasonNumber: Int): List<OfflineEpisode>

    @Query("SELECT * FROM episodes WHERE tmdbId = :tmdbId AND seasonNumber = :seasonNumber")
    fun getAllEpisodesAsFlow(tmdbId: Int, seasonNumber: Int): Flow<List<OfflineEpisode>>

    @Query(
        "SELECT EXISTS(SELECT * FROM episodes WHERE tmdbId = :tmdbId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber)"
    )
    suspend fun getEpisode(tmdbId: Int, seasonNumber: Int, episodeNumber: Int): Boolean

    @Query("DELETE FROM episodes WHERE tmdbId = :tmdbId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber")
    suspend fun deleteEpisode(tmdbId: Int, seasonNumber: Int, episodeNumber: Int)
}