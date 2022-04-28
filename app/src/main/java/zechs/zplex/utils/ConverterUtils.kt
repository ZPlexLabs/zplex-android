package zechs.zplex.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


object ConverterUtils {

    val times = listOf(
        TimeUnit.DAYS.toMillis(365),
        TimeUnit.DAYS.toMillis(30),
        TimeUnit.DAYS.toMillis(1),
        TimeUnit.HOURS.toMillis(1),
        TimeUnit.MINUTES.toMillis(1),
        TimeUnit.SECONDS.toMillis(1)
    )
    private val timesString = listOf("year", "month", "day", "hr", "min", "sec")

    @Throws(ParseException::class)
    fun toDuration(date: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        format.timeZone = TimeZone.getTimeZone(TimeZone.getDefault().toString())
        val past = format.parse(date)
        val now = Date()
        val duration = TimeUnit.MILLISECONDS.toMillis(
            now.time - Objects.requireNonNull(past).time
        )
        val response = StringBuilder()
        for (i in times.indices) {
            val current = times[i]
            val temp = duration / current
            if (temp > 0) {
                response.append(temp).append(" ").append(timesString[i])
                    .append(if (temp != 1L) "s" else "").append(" ago")
                break
            }
        }
        return if ("" == response.toString()) "0 secs ago" else response.toString()
    }

    fun getSize(size: Long): String {
        val s: String
        val kb = size.toString().toDouble() / 1024
        val mb = kb / 1024
        val gb = mb / 1024
        val tb = gb / 1024
        s = when {
            size < 1024L -> "$size B"
            size < 1024L * 1024 -> String.format("%.2f", kb) + " KB"
            size < 1024L * 1024 * 1024 -> String.format("%.2f", mb) + " MB"
            size < 1024L * 1024 * 1024 * 1024 -> String.format("%.2f", gb) + " GB"
            else -> String.format("%.2f", tb) + " TB"
        }
        return s
    }

    fun convertMinutes(min: Int): String {
        if (min <= 60) return "$min min"
        return "${min / 60} hr ${min % 60} min"
    }

    fun parseDate(
        date: String,
        srcPattern: String = "yyyy-MM-dd",
        dstPattern: String = "EEEE dd, yyyy"
    ): String {
        val srcFormat = DateTimeFormatter.ofPattern(srcPattern)
        val dstFormat = DateTimeFormatter.ofPattern(dstPattern, Locale.ENGLISH)
        val datetime = LocalDate.parse(date, srcFormat)
        return datetime.format(dstFormat)
    }

    fun yearsBetween(startDate: String, endDate: String): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val start = LocalDate.parse(startDate, formatter)
        val end = LocalDate.parse(endDate, formatter)
        return Period.between(start, end).years
    }

    fun getDate(): String {
        val time = System.currentTimeMillis()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(time)!!
    }

}