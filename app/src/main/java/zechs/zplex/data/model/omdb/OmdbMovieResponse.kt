package zechs.zplex.data.model.omdb

import androidx.annotation.Keep

/* After some analysis, the following fields have been determined to be non-null:
Actors, Country, Director, Genre, Language, Plot, Poster, Ratings, Released, Response, Runtime, Title, Type, Writer, Year, imdbID, imdbRating, imdbVotes*/
@Keep
data class OmdbMovieResponse(
    val imdbID: String,
    val imdbRating: String?,
    val imdbVotes: String,
    val Plot: String,
    val Rated: String?,
    val Released: String,
    val Runtime: String,
    val Title: String,
    val Type: String,
    val Year: String
)