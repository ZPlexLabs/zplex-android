package zechs.zplex.adapter.media

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Media

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
        val vote_average: Double?,
        val backdrop_path: String?,
        val overview: String?,
        val release_date: String?
    ) : AboutDataModel() {
        fun toMedia() = Media(
            id = id,
            media_type = media_type,
            name = name,
            poster_path = poster_path,
            title = title,
            vote_average = vote_average,
            backdrop_path = backdrop_path,
            overview = overview,
            release_date = release_date
        )
    }

    @Keep
    data class Video(
        val name: String,
        val key: String,
        val site: String,
        val thumbUrl: String,
        val watchUrl: String
    ) : AboutDataModel()

    @Keep
    data class Collection(
        val id: Int,
        val name: String?,
        val poster_path: String?,
        val backdrop_path: String?
    ) : AboutDataModel()

    @Keep
    data class Media(
        val plot: String,
        val partOf: String?
    ) : AboutDataModel()

}