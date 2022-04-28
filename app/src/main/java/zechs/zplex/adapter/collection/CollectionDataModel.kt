package zechs.zplex.adapter.collection

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Media

sealed class CollectionDataModel {

    @Keep
    data class Header(
        val title: String,
        val backdropPath: String?,
        val posterPath: String?
    ) : CollectionDataModel()

    @Keep
    data class Parts(
        val parts: List<Media>
    ) : CollectionDataModel()

}