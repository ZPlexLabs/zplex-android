package zechs.zplex.zplex_api.data.remote.api.config.model.filter


data class Filter(
    val genres: List<Genre>,
    val parentalRatings: List<String>,
    val studios: List<Studio>,
    val type: String,
    val years: List<Int>
)