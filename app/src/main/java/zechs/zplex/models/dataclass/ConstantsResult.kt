package zechs.zplex.models.dataclass

import androidx.annotation.Keep

@Keep
data class ConstantsResult(
    val zplex: String = "",
    val zplex_drive_id: String = "",
    val zplex_movies_id: String = "",
    val zplex_shows_id: String = "",
    val client_id: String = "",
    val client_secret: String = "",
    val refresh_token: String = "",
    val temp_token: String = ""
)
