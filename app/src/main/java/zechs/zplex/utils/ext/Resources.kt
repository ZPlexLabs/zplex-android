package zechs.zplex.utils.ext

import android.content.res.Resources

fun Resources.dpToPx(dp: Int): Int {
    val density = displayMetrics.density
    return (dp.toFloat() * density).toInt()
}