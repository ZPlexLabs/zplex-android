package zechs.zplex.utils.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.Excludes
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import dagger.hilt.android.EntryPointAccessors
import java.io.InputStream

@GlideModule
@Excludes(com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule::class)
class MyGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val app = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(app, GlideOkHttpEntryPoint::class.java)
        val client = entryPoint.okHttpClient()

        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(client)
        )
    }
}