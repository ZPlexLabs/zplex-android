package zechs.zplex.zplex_api.data.remote.api.config.model

import zechs.zplex.zplex_api.data.remote.api.config.model.filter.Filter

data class ConfigResponse(
    val filters: List<Filter>,
    val streamingHost: String
)