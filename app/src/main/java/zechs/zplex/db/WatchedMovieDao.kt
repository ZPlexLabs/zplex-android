package zechs.zplex.db


import androidx.lifecycle.LiveData
import androidx.room.*
import zechs.zplex.models.dataclass.WatchedMovie

@Dao
interface WatchedMovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchedMovie(watchedMovie: WatchedMovie): Long

    @Query("SELECT * FROM `watched_movies`")
    fun getAllWatchedMovies(): LiveData<List<WatchedMovie>>

    @Query(
        "SELECT * FROM `watched_movies` " +
                "WHERE tmdbId = :tmdbId " +
                "LIMIT 1"
    )
    suspend fun getWatchedMovie(tmdbId: Int): WatchedMovie?

    @Delete
    suspend fun deleteWatchedMovie(watchedMovie: WatchedMovie)

}