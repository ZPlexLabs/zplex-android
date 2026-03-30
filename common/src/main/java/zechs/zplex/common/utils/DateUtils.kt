package zechs.zplex.common.utils

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    fun parseDate(
        date: String,
        srcPattern: String = "yyyy-MM-dd",
        dstPattern: String = "EEEE dd, yyyy"
    ): String {
        val srcFormat = DateTimeFormatter.ofPattern(srcPattern)
        val dstFormat = DateTimeFormatter.ofPattern(dstPattern, Locale.ENGLISH)
        return LocalDate.parse(date, srcFormat).format(dstFormat)
    }

    fun yearsBetween(startDate: String, endDate: String): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val start = LocalDate.parse(startDate, formatter)
        val end = LocalDate.parse(endDate, formatter)
        return Period.between(start, end).years
    }

    fun today(): String {
        return LocalDate.now().toString()
    }
}