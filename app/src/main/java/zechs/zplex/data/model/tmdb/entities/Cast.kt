package zechs.zplex.data.model.tmdb.entities

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Cast(
    val character: String,
    val credit_id: String,
    val id: Int,
    val name: String,
    val profile_path: String?
) : Serializable