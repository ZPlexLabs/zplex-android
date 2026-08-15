package zechs.zplex.zplex_api.data.remote.api.me.model

import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

data class WatchlistRequest(
    val mediaType: MediaType,
    val tmdbId: Int
)

data class WatchlistItemResponse(
    val mediaType: MediaType,
    val tmdbId: Int,
    val addedAt: String
)
