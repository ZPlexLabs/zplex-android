package zechs.zplex.data.model.config

data class ConfigResponse(
    val filters: List<Filter>,
    val streamingHost: String
)