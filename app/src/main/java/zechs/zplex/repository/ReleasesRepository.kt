package zechs.zplex.repository

import zechs.zplex.api.RetrofitInstance

class ReleasesRepository {
    suspend fun getReleaseLogs() = RetrofitInstance.api_witch.getZPlexReleases()
}