package zechs.zplex

import android.app.Application
import android.content.Context

class ThisApp : Application() {
    override fun onCreate() {
        instance = this
        super.onCreate()
    }

    companion object {
        var instance: ThisApp? = null
            private set

        val context: Context?
            get() = instance
    }
}
