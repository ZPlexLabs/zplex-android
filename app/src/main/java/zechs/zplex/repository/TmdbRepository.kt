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
    ) = RetrofitInstance.tmdbApi.getShow(tvId)

    suspend fun getMovie(
        movieId: Int
    ) = RetrofitInstance.tmdbApi.getMovie(movieId)

    suspend fun getSeason(
        tvId: Int,
        seasonNumber: Int
    ) = RetrofitInstance.tmdbApi.getSeason(tvId, seasonNumber)

    suspend fun getPopularMovie(
        year: Int,
    ) = RetrofitInstance.tmdbApi.getDiscover(
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
    ) = RetrofitInstance.tmdbApi.getDiscover(
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
    ) = RetrofitInstance.tmdbApi.getEpisode(tvId, seasonNumber, episodeNumber)

    suspend fun getSearch(
        query: String,
        page: Int
    ) = RetrofitInstance.tmdbApi.getSearch(query = query, page = page)

    suspend fun getCollection(
        collectionId: Int
    ) = RetrofitInstance.tmdbApi.getCollection(collection_id = collectionId)

    suspend fun getCredit(
        creditId: String
    ) = RetrofitInstance.tmdbApi.getCredit(credit_id = creditId)

    suspend fun getPeople(
        person_id: Int
    ) = RetrofitInstance.tmdbApi.getPeople(person_id = person_id)

    suspend fun getTrending(
        time_window: String
    ) = RetrofitInstance.tmdbApi.getTrending(time_window)


    suspend fun getUpcoming(page: Int) = RetrofitInstance.tmdbApi.getUpcoming(page = page)

    suspend fun getBrowse(
        mediaType: MediaType,
        sortBy: SortBy,
        order: Order,
        page: Int,
        withKeyword: Int?,
        withGenres: Int?,
    ) = RetrofitInstance.tmdbApi.getBrowse(
        media_type = mediaType,
        sort_by = "${sortBy.name}.${order.name}",
        page = page,
        with_keywords = withKeyword,
        with_genres = withGenres,
    )

    suspend fun getInTheatres(
        dateStart: String, dateEnd: String
    ) = RetrofitInstance.tmdbApi.getInTheatres(
        release_date_start = dateStart,
        release_date_end = dateEnd
    )

    suspend fun getPopularOnStreaming(
    ) = RetrofitInstance.tmdbApi.getPopularOnStreaming()

    suspend fun getShowsFromCompany(
        company_id: Int,
        page: Int
    ) = RetrofitInstance.tmdbApi.getFromCompany(
        media_type = MediaType.tv,
        with_companies = company_id,
        page = page
    )

    suspend fun getMoviesFromCompany(
        company_id: Int,
        page: Int
    ) = RetrofitInstance.tmdbApi.getFromCompany(
        media_type = MediaType.movie,
        with_companies = company_id,
        page = page
    )

}