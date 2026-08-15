package zechs.zplex.zplex_api.data.remote.api.me.model

import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

data class PlayedRequest(
    val mediaType: MediaType,
    val tmdbId: Int,
    val seasonNumber: Int?,
    val episodeNumber: Int?
)

data class PlayedResponse(
    val mediaType: MediaType,
    val tmdbId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val playedAt: String
)
