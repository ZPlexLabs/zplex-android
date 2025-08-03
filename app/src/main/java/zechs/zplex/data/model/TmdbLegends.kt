package zechs.zplex.data.model

import androidx.annotation.Keep


enum class BackdropSize {
    w300,
    w780,
    w1280,
    original
}

enum class PosterSize {
    w92,
    w154,
    w185,
    w342,
    w500,
    w780,
    original
}

enum class ProfileSize {
    w45,
    w185,
    h632,
    original
}

enum class StillSize {
    w92,
    w185,
    w300,
    original
}


enum class SortBy {
    popularity,
    release_date,
    revenue,
    primary_release_date,
    original_title,
    vote_average,
    vote_count
}

enum class Order {
    asc, desc
}

@Keep
enum class MediaType {
    tv, movie, person, collection
}