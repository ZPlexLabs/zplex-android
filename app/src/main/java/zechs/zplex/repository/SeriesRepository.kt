package zechs.zplex.repository

import zechs.zplex.api.RetrofitInstance

class SeriesRepository {

    suspend fun getSeries(
        series_id: Int
    ) = RetrofitInstance.api_tvdb.getSeries(series_id)

}