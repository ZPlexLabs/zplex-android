package zechs.zplex.ui.collection.adapter

import androidx.annotation.Keep
import zechs.zplex.data.model.tmdb.entities.Media

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