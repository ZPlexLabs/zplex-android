package zechs.zplex.repository

import zechs.zplex.api.RetrofitInstance

class TmdbRepository {

    suspend fun getMovies(
        movies_id: Int
    ) = RetrofitInstance.api_tmdb.getMovies(movies_id)

    suspend fun getCredits(
        movies_id: Int
    ) = RetrofitInstance.api_tmdb.getCredits(movies_id)
}