package zechs.zplex

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import zechs.zplex.utils.NotificationKeys

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
