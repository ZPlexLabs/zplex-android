package zechs.zplex.data.model.tmdb.entities

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Season(
    val episode_count: Int,
    val id: Int,
    val name: String,
    val poster_path: String?,
    val season_number: Int,
    val overview: String?,
    val air_date: String?
) : Parcelable