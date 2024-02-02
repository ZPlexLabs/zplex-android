package zechs.zplex

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import zechs.zplex.utils.CrashHandler

@HiltAndroidApp
class ThisApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(applicationContext, oldHandler))
    }

}