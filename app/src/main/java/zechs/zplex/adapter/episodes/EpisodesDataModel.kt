package zechs.zplex.adapter.episodes

import androidx.annotation.Keep
import zechs.zplex.models.zplex.Episode

sealed class EpisodesDataModel {

    @Keep
    data class Header(
        val seasonNumber: String,
        val seasonName: String?,
        val seasonPosterPath: String?,
        val seasonOverview: String
    ) : EpisodesDataModel()

    @Keep
    data class Episodes(
        val episodes: List<Episode>,
        val accessToken: String?
    ) : EpisodesDataModel()

}