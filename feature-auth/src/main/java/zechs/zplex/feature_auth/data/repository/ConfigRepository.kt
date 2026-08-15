package zechs.zplex.feature_auth.data.repository

import kotlinx.coroutines.flow.Flow
import zechs.zplex.common.utils.Result
import zechs.zplex.common.utils.SafeApiCaller
import zechs.zplex.zplex_api.data.local.config.ConfigStore
import zechs.zplex.zplex_api.data.remote.api.config.ConfigApi
import zechs.zplex.zplex_api.data.remote.api.config.model.Capability
import zechs.zplex.zplex_api.data.remote.api.config.model.ConfigResponse
import javax.inject.Inject

class ConfigRepository @Inject constructor(
    private val configApi: ConfigApi,
    private val configStore: ConfigStore,
    private val safeApiCaller: SafeApiCaller
) {

    val cachedConfig: Flow<ConfigResponse?> = configStore.config

    suspend fun config(): Result<ConfigResponse> =
        safeApiCaller.call { configApi.config() }.also { result ->
            if (result is Result.Success) configStore.save(result.data)
        }

    suspend fun capabilities(): Result<List<Capability>> =
        safeApiCaller.call { configApi.capabilities() }
}