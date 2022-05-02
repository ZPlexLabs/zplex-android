package zechs.zplex.utils

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.ViewGroup
import android.widget.TextView
import androidx.transition.TransitionManager

object SpannableTextView {
    fun spannablePlotText(
        textView: TextView, plot: String,
        limit: Int, suffixText: String,
        root: ViewGroup
    ) {
        val textColor = ForegroundColorSpan(Color.parseColor("#BDFFFFFF"))
        val suffixColor = ForegroundColorSpan(Color.parseColor("#DEFFFFFF"))

        if (plot.length > 200) {
            val stringBuilder = SpannableStringBuilder()

            val plotText = SpannableString(plot.substring(0, limit)).apply {
                setSpan(textColor, 0, limit, 0)
            }

            val readMore = SpannableString(suffixText).apply {
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, suffixText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(suffixColor, 0, suffixText.length, 0)
            }

            stringBuilder.append(plotText)
            stringBuilder.append(readMore)

            val textViewTag = "textViewTAG"
            textView.apply {
                if (tag != null) {
                    text = plot
                } else {
                    setText(stringBuilder, TextView.BufferType.SPANNABLE)
                }
                setOnClickListener {
                    TransitionManager.beginDelayedTransition(root)
                    if (text.length > (limit + suffixText.length)) {
                        setText(stringBuilder, TextView.BufferType.SPANNABLE)
                        tag = null
                    } else {
                        text = plot
                        tag = textViewTag
                    }
                }
            }
        } else {
            textView.text = plot
            textView.setOnClickListener(null)
        }
    }
}