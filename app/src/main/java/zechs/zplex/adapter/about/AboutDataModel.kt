package zechs.zplex.adapter.about

import androidx.annotation.Keep

sealed class AboutDataModel {

    @Keep
    data class Header(
        val heading: String?
    ) : AboutDataModel()

    @Keep
    data class Season(
        val episode_count: Int,
        val id: Int,
        val name: String,
        val poster_path: String?,
        val season_number: Int
    ) : AboutDataModel()

    @Keep
    data class Collection(
        val backdrop_path: String?,
        val id: Int,
        val name: String,
        val poster_path: String?
    ) : AboutDataModel()

    @Keep
    data class Cast(
        val character: String,
        val credit_id: String,
        val person_id: Int,
        val name: String,
        val profile_path: String?
    ) : AboutDataModel()

    @Keep
    data class Curation(
        val id: Int,
        val media_type: String?,
        val name: String?,
        val poster_path: String?,
        val title: String?,
        val vote_average: Double?
    ) : AboutDataModel()

    @Keep
    data class Video(
        val name: String,
        val key: String,
        val site: String,
        val thumbUrl: String,
        val watchUrl: String
    ) : AboutDataModel()

}