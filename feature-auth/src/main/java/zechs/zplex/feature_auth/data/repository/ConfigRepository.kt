package zechs.zplex.feature_auth.data.repository

import zechs.zplex.common.utils.Result
import zechs.zplex.common.utils.SafeApiCaller
import zechs.zplex.zplex_api.data.remote.api.config.ConfigApi
import zechs.zplex.zplex_api.data.remote.api.config.model.Capability
import zechs.zplex.zplex_api.data.remote.api.config.model.ConfigResponse
import javax.inject.Inject

class ConfigRepository @Inject constructor(
    private val configApi: ConfigApi,
    private val safeApiCaller: SafeApiCaller
) {

    suspend fun config(): Result<ConfigResponse> =
        safeApiCaller.call { configApi.config() }

    suspend fun capabilities(): Result<List<Capability>> =
        safeApiCaller.call { configApi.capabilities() }
}