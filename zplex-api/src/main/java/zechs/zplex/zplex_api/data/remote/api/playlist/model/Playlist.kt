package zechs.zplex.zplex_api.data.remote.api.playlist.model

import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

data class PlaylistResponse(
    val id: Long,
    val name: String,
    val createdAt: String,
    val updatedAt: String
)

data class PlaylistDetailResponse(
    val id: Long,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val items: List<PlaylistItemResponse>
)

data class PlaylistItemResponse(
    val id: Long,
    val mediaType: MediaType,
    val tmdbId: Int,
    val position: Int,
    val addedAt: String
)

data class PlaylistNameRequest(
    val name: String
)

data class PlaylistItemRequest(
    val mediaType: MediaType,
    val tmdbId: Int
)

data class PlaylistReorderRequest(
    val itemIds: List<Long>
)
