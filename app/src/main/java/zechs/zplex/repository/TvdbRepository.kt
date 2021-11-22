package zechs.zplex.repository

import zechs.zplex.api.RetrofitInstance

class TvdbRepository {

    suspend fun getSeries(
        series_id: Int
    ) = RetrofitInstance.api_tvdb.getSeries(series_id)

    suspend fun getActors(
        series_id: Int
    ) = RetrofitInstance.api_tvdb.getActors(series_id)

    suspend fun getEpisodes(
        series_id: Int
    ) = RetrofitInstance.api_tvdb.getEpisodes(series_id)

}