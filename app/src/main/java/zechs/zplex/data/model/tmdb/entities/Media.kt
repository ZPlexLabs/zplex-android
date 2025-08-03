package zechs.zplex.data.model.tmdb.entities

import androidx.annotation.Keep
import zechs.zplex.data.model.MediaType
import zechs.zplex.utils.util.Converter
import java.io.Serializable

@Keep
data class Media(
    val id: Int,
    val media_type: MediaType?,
    val name: String?,
    val poster_path: String?,
    val title: String?,
    val vote_average: Double?,
    val backdrop_path: String?,
    val overview: String?,
    val release_date: String?,
    val first_air_date: String?,
    val fileId: String? = null,
    val modifiedTime: Long? = null
) : Serializable {

    fun releasedDate(): String? {
        val date = release_date ?: first_air_date
        return if (date != null && date != "") {
            Converter.parseDate(date, dstPattern = "MMM dd, yyyy")
        } else null
    }

}