package zechs.zplex.zplex_api.data.repository

import zechs.zplex.common.utils.Result
import zechs.zplex.common.utils.SafeApiCaller
import zechs.zplex.zplex_api.data.remote.api.stream.StreamApi
import javax.inject.Inject

data class StreamUrl(
    val url: String,
    val grantToken: String
)

class StreamRepository @Inject constructor(
    private val streamApi: StreamApi,
    private val safeApiCaller: SafeApiCaller
) {

    suspend fun getStreamUrl(fileId: String, streamingHost: String): Result<StreamUrl> {
        return when (val grantResult = safeApiCaller.call { streamApi.getStreamGrant(fileId) }) {
            is Result.Success -> {
                val url = "$streamingHost/api/stream/$fileId"
                Result.Success(StreamUrl(url, grantResult.data.grant))
            }
            is Result.Error -> grantResult
        }
    }
}