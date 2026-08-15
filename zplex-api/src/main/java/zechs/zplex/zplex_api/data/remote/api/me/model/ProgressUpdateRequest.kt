package zechs.zplex.zplex_api.data.remote.api.me.model

import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

data class ProgressUpdateRequest(
    val mediaType: MediaType,
    val tmdbId: Int,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val progressMs: Long,
    val durationMs: Long
)
