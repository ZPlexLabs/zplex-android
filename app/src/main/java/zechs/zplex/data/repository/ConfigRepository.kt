package zechs.zplex.data.repository

import zechs.zplex.data.model.config.Capability
import zechs.zplex.data.model.config.ConfigResponse
import zechs.zplex.data.remote.ZPlexApi
import zechs.zplex.utils.SafeApiCaller
import zechs.zplex.utils.state.Result
import javax.inject.Inject

class ConfigRepository @Inject constructor(
    private val api: ZPlexApi,
    private val safeApiCaller: SafeApiCaller
) {

    suspend fun config(): Result<ConfigResponse> =
        safeApiCaller.call { api.config() }

    suspend fun capabilities(): Result<List<Capability>> =
        safeApiCaller.call { api.capabilities() }
}