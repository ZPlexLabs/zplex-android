package zechs.zplex.adapter.streams

import androidx.annotation.Keep

sealed class StreamsDataModel {

    @Keep
    data class Original(
        val title: String,
        val id: String,
    ) : StreamsDataModel()

    @Keep
    data class Stream(
        val name: String,
        val url: String,
        val cookie: String
    ) : StreamsDataModel()

    object Loading : StreamsDataModel()
}