package zechs.zplex.repository

import retrofit2.Response
import zechs.zplex.api.RetrofitInstance
import zechs.zplex.models.zplex.MovieResponse

class ZPlexRepository {

    suspend fun getMovie(tmdbId: Int): Response<MovieResponse> {
        return RetrofitInstance.zplexApi.getMovie(tmdbId)
    }

    suspend fun getSeason(
        tmdbId: Int,
        seasonNumber: Int
    ) = RetrofitInstance.zplexApi.getSeason(
        tmdb_id = tmdbId,
        season = seasonNumber
    )

}