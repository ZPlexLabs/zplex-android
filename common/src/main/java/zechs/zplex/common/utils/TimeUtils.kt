package zechs.zplex.common.utils

import java.util.concurrent.TimeUnit

object TimeUtils {
    private val times = listOf(
        TimeUnit.DAYS.toMillis(365),
        TimeUnit.DAYS.toMillis(30),
        TimeUnit.DAYS.toMillis(1),
        TimeUnit.HOURS.toMillis(1),
        TimeUnit.MINUTES.toMillis(1),
        TimeUnit.SECONDS.toMillis(1)
    )

    private val labels = listOf("year", "month", "day", "hr", "min", "sec")

    fun toDuration(pastMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val duration = nowMillis - pastMillis

        for (i in times.indices) {
            val value = duration / times[i]
            if (value >= 1) {
                return "$value ${labels[i]}${if (value > 1) "s" else ""} ago"
            }
        }
        return "0 secs ago"
    }

    fun minutesToReadable(min: Int): String {
        if (min <= 60) return "$min min"
        return "${min / 60} hr ${min % 60} min"
    }
}