package zechs.zplex.data.model.config

data class Filter(
    val genres: List<Genre>,
    val parentalRatings: List<String>,
    val studios: List<Studio>,
    val type: String,
    val years: List<Int>
)