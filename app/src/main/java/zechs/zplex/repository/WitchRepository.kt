package zechs.zplex.repository

import zechs.zplex.api.RetrofitInstance

class WitchRepository {

    suspend fun requestMovie(
        imdbId: String,
        tmdbId: String,
        deviceId: String
    ) = RetrofitInstance.api_witch.requestMovie(imdbId, tmdbId, deviceId)

    suspend fun getDashVideos(
        fileId: String
    ) = RetrofitInstance.api_witch.getDashVideos(fileId)
}