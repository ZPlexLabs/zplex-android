package zechs.zplex.models.tmdb.entities

import androidx.annotation.Keep
import zechs.zplex.adapter.media.AboutDataModel
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
    val release_date: String?
) : Serializable {

    fun toCuration() = AboutDataModel.Curation(
        id = id,
        media_type = media_type,
        name = name,
        poster_path = poster_path,
        title = title,
        vote_average = vote_average,
        backdrop_path = backdrop_path,
        overview = overview,
        release_date = release_date
    )
}