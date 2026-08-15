package zechs.zplex.zplex_api.data.repository

import zechs.zplex.common.utils.Result
import zechs.zplex.common.utils.SafeApiCaller
import zechs.zplex.zplex_api.data.remote.api.admin.AdminApi
import zechs.zplex.zplex_api.data.remote.api.admin.model.BlacklistRequest
import zechs.zplex.zplex_api.data.remote.api.admin.model.UpdateCapabilityRequest
import zechs.zplex.zplex_api.data.remote.api.admin.model.UserAccessRequest
import zechs.zplex.zplex_api.data.remote.api.admin.model.UserSummaryResponse
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import javax.inject.Inject

class AdminRepository @Inject constructor(
    private val api: AdminApi,
    private val safeApiCaller: SafeApiCaller
) {

    suspend fun users(): Result<List<UserSummaryResponse>> =
        safeApiCaller.call { api.users() }

    suspend fun updateCapabilities(username: String, capabilities: List<Int>): Result<Unit> =
        safeApiCaller.callUnit { api.updateCapabilities(username, UpdateCapabilityRequest(capabilities)) }

    suspend fun deleteUser(username: String): Result<Unit> =
        safeApiCaller.callUnit { api.deleteUser(username) }

    suspend fun updateAccess(username: String, request: UserAccessRequest): Result<Unit> =
        safeApiCaller.callUnit { api.updateAccess(username, request) }

    suspend fun addBlacklist(username: String, mediaType: MediaType, tmdbId: Int): Result<Unit> =
        safeApiCaller.callUnit { api.addBlacklist(username, BlacklistRequest(mediaType, tmdbId)) }

    suspend fun removeBlacklist(username: String, mediaType: MediaType, tmdbId: Int): Result<Unit> =
        safeApiCaller.callUnit { api.removeBlacklist(username, mediaType, tmdbId) }
}
