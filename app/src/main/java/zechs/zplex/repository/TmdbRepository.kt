package zechs.zplex.repository

import zechs.zplex.api.RetrofitInstance
import zechs.zplex.db.WatchlistDatabase
import zechs.zplex.models.tmdb.entities.Media

class TmdbRepository(
    private val db: WatchlistDatabase
) {

    suspend fun upsertMedia(
        media: Media
    ) = db.getWatchlistDao().upsertMedia(media)

    fun getMedia(
        id: Int
    ) = db.getWatchlistDao().getMedia(id)

    suspend fun deleteMedia(
        media: Media
    ) = db.getWatchlistDao().deleteMedia(media)

    fun getSavedMedia() = db.getWatchlistDao().getAllMedia()

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

    suspend fun getBrowse(
        mediaType: String,
        sortBy: String,
        page: Int,
        withKeyword: Int?,
        withGenres: Int?,
    ) = RetrofitInstance.api_tmdb.getDiscover(
        media_type = mediaType,
        sort_by = sortBy,
        page = page,
        with_keywords = withKeyword,
        with_genres = withGenres,
        first_air_date_year = null
    )

    suspend fun getTrending() = RetrofitInstance.api_tmdb.getTrendingToday()

}