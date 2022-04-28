package zechs.zplex.models.tmdb.entities

import androidx.annotation.Keep
import zechs.zplex.utils.ConverterUtils
import java.io.Serializable

@Keep
data class Media(
    val id: Int,
    val media_type: String?,
    val name: String?,
    val poster_path: String?,
    val title: String?,
    val vote_average: Double?,
    val backdrop_path: String?,
    val overview: String?,
    val release_date: String?,
    val first_air_date: String?
) : Serializable {
    fun releasedDate(): String? {
        val date = release_date ?: first_air_date
        return if (date != null && date != "") {
            ConverterUtils.parseDate(date, dstPattern = "MMM dd, yyyy")
        } else null
    }
}