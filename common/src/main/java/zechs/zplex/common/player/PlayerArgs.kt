package zechs.zplex.common.player

import java.io.Serializable

data class PlayerItem(
    val fileId: String,
    val tmdbId: Int,
    val isTv: Boolean,
    val title: String,
    val subtitle: String?,
    val seasonNumber: Int,
    val episodeNumber: Int
) : Serializable

data class PlayerArgs(
    val items: List<PlayerItem>,
    val startIndex: Int,
    val startPositionMs: Long
) : Serializable {
    companion object {
        const val EXTRA = "zplex.player.args"
    }
}
