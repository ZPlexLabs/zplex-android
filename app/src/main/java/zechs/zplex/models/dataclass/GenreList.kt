package zechs.zplex.models.dataclass

import android.graphics.drawable.Drawable
import androidx.annotation.Keep

@Keep
data class GenreList(
    val id: Int?,
    val name: String,
    val mediaType: String?,
    val keyword: Int? = null,
    val icon: Drawable?
)