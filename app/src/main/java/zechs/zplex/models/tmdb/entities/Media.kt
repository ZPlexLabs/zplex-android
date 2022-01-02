package zechs.zplex.models.tmdb.entities

import androidx.annotation.Keep
import java.io.Serializable


@Keep
data class Media(
    val id: Int,
    val media_type: String?,
    val name: String?,
    val poster_path: String?,
    val title: String?,
    val vote_average: Double?
) : Serializable