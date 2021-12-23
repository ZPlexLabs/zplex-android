package zechs.zplex.repository

import zechs.zplex.api.RetrofitInstance

class TmdbRepository {

    suspend fun getMovies(
        movies_id: Int
    ) = RetrofitInstance.api_tmdb.getMovies(movies_id)

    suspend fun getCredits(
        movies_id: Int
    ) = RetrofitInstance.api_tmdb.getCredits(movies_id)

    suspend fun getShow(
        tvId: Int
    ) = RetrofitInstance.api_tmdb.getShow(tvId)

    suspend fun getSeason(
        tvId: Int,
        seasonNumber: Int
    ) = RetrofitInstance.api_tmdb.getSeason(tvId, seasonNumber)

    suspend fun getPopularMovie(
        year: Int
    ) = RetrofitInstance.api_tmdb.getPopularMovies(first_air_date_year = year)

    suspend fun getPopularShow(
        year: Int,
        keyword: String
    ) = RetrofitInstance.api_tmdb.getPopularShows(
        first_air_date_year = year,
        with_keywords = keyword
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

    suspend fun getMovie(
        movies_id: Int
    ) = RetrofitInstance.api_tmdb.getMovie(movies_id)

    suspend fun getCollection(
        collection_id: Int
    ) = RetrofitInstance.api_tmdb.getCollection(collection_id = collection_id)

    suspend fun getCredit(
        credit_id: String
    ) = RetrofitInstance.api_tmdb.getCredit(credit_id = credit_id)

    suspend fun getPeople(
        person_id: Int
    ) = RetrofitInstance.api_tmdb.getPeople(person_id = person_id)

    suspend fun getTrending() = RetrofitInstance.api_tmdb.getTrendingToday()

    suspend fun getBrowse(
        mediaType: String,
        sortBy: String,
        page: Int,
        withKeyword: Int?,
        withGenres: Int,
    ) = RetrofitInstance.api_tmdb.getDiscover(
        media_type = mediaType,
        sort_by = sortBy,
        page = page,
        with_keywords = withKeyword,
        with_genres = withGenres
    )
}