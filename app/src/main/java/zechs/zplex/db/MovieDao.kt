package zechs.zplex.db

import androidx.lifecycle.LiveData
import androidx.room.*
import zechs.zplex.models.dataclass.Movie

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovie(media: Movie): Long

    @Query("SELECT * FROM movies")
    fun getAllMovies(): LiveData<List<Movie>>

    @Query("SELECT EXISTS(SELECT * FROM movies WHERE id = :id)")
    fun getMovie(id: Int): LiveData<Boolean>

    @Delete
    suspend fun deleteMovie(media: Movie)

}