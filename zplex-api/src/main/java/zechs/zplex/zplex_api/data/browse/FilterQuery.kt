package zechs.zplex.zplex_api.data.browse

/** Selected browse filters; renders to the backend filterBy grammar. */
data class FilterQuery(
    val genreIds: Set<Int> = emptySet(),
    val parentalRatings: Set<String> = emptySet(),
    val studioIds: Set<Int> = emptySet(),
    val years: Set<Int> = emptySet()
) {
    val isEmpty: Boolean
        get() = genreIds.isEmpty() && parentalRatings.isEmpty() &&
            studioIds.isEmpty() && years.isEmpty()

    val selectionCount: Int
        get() = genreIds.size + parentalRatings.size + studioIds.size + years.size

    fun toFilterBy(): String {
        val parts = buildList {
            if (genreIds.isNotEmpty()) add("genres(${genreIds.joinToString(", ")})")
            if (parentalRatings.isNotEmpty()) {
                add("parentalRatings(${parentalRatings.joinToString(", ") { "\"$it\"" }})")
            }
            if (studioIds.isNotEmpty()) add("studios(${studioIds.joinToString(", ")})")
            if (years.isNotEmpty()) add("years(${years.joinToString(", ")})")
        }
        return parts.joinToString(", ")
    }
}
