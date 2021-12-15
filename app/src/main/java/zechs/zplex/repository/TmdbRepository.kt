package zechs.zplex.repository

import zechs.zplex.api.RetrofitInstance

class TmdbRepository {

    suspend fun getMovie(
        movies_id: Int
    ) = RetrofitInstance.api_tmdb.getMovie(movies_id)

    suspend fun getCredits(
        movies_id: Int
    ) = RetrofitInstance.api_tmdb.getCredits(movies_id)

}