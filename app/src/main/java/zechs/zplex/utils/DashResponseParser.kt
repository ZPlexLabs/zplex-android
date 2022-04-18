package zechs.zplex.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import zechs.zplex.models.drive.DashResponse
import zechs.zplex.models.drive.Stream
import java.net.URLDecoder


object DashResponseParser {

    private val gson by lazy { GsonBuilder().disableHtmlEscaping().create() }

    private enum class Reason(val value: String) {
        NO_PERMISSION("You don't have permission to access this video."),
        NOT_FORMATTED("Video could not be formatted yet. Check back later."),
        STILL_PROCESSING("We're processing this video. Please check back later."),
        VIDEO_DOEST_EXIST("This video doesn't exist.");
    }

    private enum class Response(val value: String) {
        VIDEO_WILL_NOT_BE_FORMATTED("Video will be not formatted"),
        VIDEO_STILL_PROCESSING("Video is still processing"),
        BAD_RESPONSE("Bad response from server."),
        SOMETHING_WENT_WRONG("Something went wrong."),
    }

    fun parse(response: String?, cookie: String): DashResponse {
        if (response == null) {
            return DashResponse(
                error = Response.BAD_RESPONSE.value,
                streams = emptyList()
            )
        }

        val json = parseToJson(response)

        return when (json.get("status").asString) {
            "ok" -> {
                DashResponse(
                    error = null,
                    streams = filterVideoStreams(json, cookie)
                )
            }
            "fail" -> {
                when (json.get("reason").asString) {
                    Reason.NO_PERMISSION.value -> {
                        DashResponse(
                            error = Reason.NO_PERMISSION.value,
                            streams = emptyList()
                        )
                    }
                    Reason.NOT_FORMATTED.value -> {
                        DashResponse(
                            error = Response.VIDEO_WILL_NOT_BE_FORMATTED.value,
                            streams = emptyList()
                        )
                    }
                    Reason.STILL_PROCESSING.value -> {
                        DashResponse(
                            error = Response.VIDEO_STILL_PROCESSING.value,
                            streams = emptyList()
                        )
                    }
                    Reason.VIDEO_DOEST_EXIST.value -> {
                        DashResponse(
                            error = Reason.VIDEO_DOEST_EXIST.value,
                            streams = emptyList()
                        )
                    }
                    else -> {
                        DashResponse(
                            error = Response.SOMETHING_WENT_WRONG.value,
                            streams = emptyList()
                        )
                    }
                }

            }
            else -> {
                DashResponse(
                    error = Response.SOMETHING_WENT_WRONG.value,
                    streams = emptyList()
                )
            }
        }

    }

    private fun filterVideoStreams(
        json: JsonObject, cookie: String
    ): List<Stream> {
        val fmtList = json.get("fmt_list").asString.split(",")
        val fmtStreamMap = json.get("fmt_stream_map").asString.split(",")
        val streamFormats = mutableListOf<Stream>()

        fmtList.forEach { fmt ->
            val data = fmt.split("/")
            fmtStreamMap.forEach { fmtMap ->
                val streamData = fmtMap.split("|")
                if (streamData[0] == data[0]) {
                    streamFormats.add(
                        Stream(
                            resolution = data[1],
                            url = streamData[1],
                            quality = dimensionToQuality(data[1]),
                            driveStream = cookie
                        )
                    )
                }
            }
        }

        return streamFormats
    }

    private fun dimensionToQuality(dimensions: String): String {
        val width = dimensions.split("x")[0].toInt()
        // val height = dimension[1].toInt()
        return when {
            width >= 1920 -> "1080p"
            width >= 1280 -> "720p"
            width >= 854 -> "480p"
            width >= 640 -> "360p"
            width >= 426 -> "240p"
            else -> "144p"
        }
    }

    private fun parseToJson(response: String): JsonObject {
        val params: MutableMap<String, String> = LinkedHashMap()

        for (param in response.split("&")) {
            val keyValue = param.split("=", limit = 2).toTypedArray()
            val key = URLDecoder.decode(keyValue[0], "UTF-8")
            val value = if (keyValue.size > 1) {
                URLDecoder.decode(keyValue[1], "UTF-8")
            } else ""
            if (key.isNotEmpty()) {
                params[key] = value
            }
        }

        val json = gson.toJson(params)
        val jsonObject = gson.fromJson(json, JsonObject::class.java)
        return jsonObject.asJsonObject
    }

}