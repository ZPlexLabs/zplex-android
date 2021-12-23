package zechs.zplex.adapter.media

import androidx.annotation.Keep
import zechs.zplex.adapter.about.AboutDataModel
import zechs.zplex.models.misc.Pairs

sealed class MediaDataModel {

    @Keep
    data class Meta(
        val title: String,
        val mediaType: String?,
        val overview: String?,
        val posterUrl: String?,
        val tmdbId: Int,
        val misc: List<Pairs>,
    ) : MediaDataModel()

    @Keep
    data class Details(
        val header: String,
        val items: List<AboutDataModel>
    ) : MediaDataModel()

}