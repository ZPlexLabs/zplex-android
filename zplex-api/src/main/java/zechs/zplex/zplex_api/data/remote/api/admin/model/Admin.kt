package zechs.zplex.zplex_api.data.remote.api.admin.model

import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

data class UserSummaryResponse(
    val username: String,
    val firstName: String,
    val lastName: String,
    val capabilities: List<Int>,
    val isAdult: Boolean,
    val allowedLibraries: List<Int>,
    val maxRatingRank: Int,
    val allowUnrated: Boolean,
    val blacklist: List<BlacklistEntry>
)

data class BlacklistEntry(
    val mediaType: MediaType,
    val tmdbId: Int
)

data class UpdateCapabilityRequest(
    val capabilities: List<Int>
)

data class UserAccessRequest(
    val allowedLibraries: List<Int>,
    val maxRatingRank: Int,
    val allowUnrated: Boolean
)

data class BlacklistRequest(
    val mediaType: MediaType,
    val tmdbId: Int
)
