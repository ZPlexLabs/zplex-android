package zechs.zplex.repository

import zechs.zplex.api.RetrofitInstance
import zechs.zplex.db.WatchlistDatabase
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.models.enum.MediaType
import zechs.zplex.models.enum.Order
import zechs.zplex.models.enum.SortBy

class TmdbRepository(
    private val db: WatchlistDatabase
) {

    suspend fun upsertMovie(
        movie: Movie
    ) = db.getMovieDao().upsertMovie(movie)

    fun fetchMovie(
        id: Int
    ) = db.getMovieDao().getMovie(id)

    suspend fun deleteMovie(
        movie: Movie
    ) = db.getMovieDao().deleteMovie(movie)

    fun getSavedMovies() = db.getMovieDao().getAllMovies()

    suspend fun upsertShow(
        Show: Show
    ) = db.getShowDao().upsertShow(Show)

    fun fetchShow(
        id: Int
    ) = db.getShowDao().getShow(id)

    suspend fun deleteShow(
        Show: Show
    ) = db.getShowDao().deleteShow(Show)

    fun getSavedShows() = db.getShowDao().getAllShows()

    suspend fun getShow(
        tvId: Int
    ) = RetrofitInstance.api_tmdb.getShow(tvId)

    suspend fun getMovie(
        movieId: Int
    ) = RetrofitInstance.api_tmdb.getMovie(movieId)

    suspend fun getSeason(
        tvId: Int,
        seasonNumber: Int
    ) = RetrofitInstance.api_tmdb.getSeason(tvId, seasonNumber)

    suspend fun getPopularMovie(
        year: Int,
    ) = RetrofitInstance.api_tmdb.getDiscover(
        media_type = "movie",
        sort_by = "popularity.desc",
        page = 1,
        with_keywords = null,
        with_genres = null,
        first_air_date_year = year
    )

    suspend fun getPopularShow(
        year: Int,
        keyword: Int?
    ) = RetrofitInstance.api_tmdb.getDiscover(
        media_type = "tv",
        sort_by = "popularity.desc",
        page = 1,
        with_keywords = keyword,
        with_genres = null,
        first_air_date_year = year
    )

    suspend fun getEpisode(
        tvId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ) = RetrofitInstance.api_tmdb.getEpisode(tvId, seasonNumber, episodeNumber)

    suspend fun getSearch(
        query: String,
        page: Int
    ) = RetrofitInstance.api_tmdb.getSearch(query = query, page = page)

    suspend fun getCollection(
        collectionId: Int
    ) = RetrofitInstance.api_tmdb.getCollection(collection_id = collectionId)

    suspend fun getCredit(
        creditId: String
    ) = RetrofitInstance.api_tmdb.getCredit(credit_id = creditId)

    suspend fun getPeople(
        person_id: Int
    ) = RetrofitInstance.api_tmdb.getPeople(person_id = person_id)

    suspend fun getTrending(
        time_window: String
    ) = RetrofitInstance.api_tmdb.getTrending(time_window)

    suspend fun getStreaming() = RetrofitInstance.api_tmdb.getStreaming()
    suspend fun getInTheatres() = RetrofitInstance.api_tmdb.getInTheatres()
    suspend fun getUpcoming(page: Int) = RetrofitInstance.api_tmdb.getUpcoming(page = page)

    suspend fun getBrowse(
        mediaType: MediaType,
        sortBy: SortBy,
        order: Order,
        page: Int,
        withKeyword: Int?,
        withGenres: Int?,
    ) = RetrofitInstance.api_tmdb.getBrowse(
        media_type = mediaType,
        sort_by = "${sortBy.name}.${order.name}",
        page = page,
        with_keywords = withKeyword,
        with_genres = withGenres,
    )
}