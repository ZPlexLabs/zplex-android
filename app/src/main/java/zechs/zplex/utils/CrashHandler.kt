package zechs.zplex.utils

import android.content.Context
import android.content.Intent
import zechs.zplex.ui.error.ErrorActivity
import zechs.zplex.utils.ext.ifNullOrEmpty
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class CrashHandler(
    private val context: Context,
    private val oldHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, exception: Throwable) {

        val stackTrace = StringWriter()
        exception.printStackTrace(PrintWriter(stackTrace))

        Intent(context.applicationContext, ErrorActivity::class.java).apply {
            putExtra(
                ErrorActivity.EXTRA_ERROR_MESSAGE,
                exception.message?.ifNullOrEmpty { "Unknown error!" })
            putExtra(ErrorActivity.EXTRA_STACK_TRACE, stackTrace.toString())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { context.applicationContext.startActivity(it) }

        oldHandler?.uncaughtException(thread, exception) ?: exitProcess(2)
    }
}