package zechs.zplex.zplex_api.data.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zechs.zplex.zplex_api.data.repository.DownloadRepository

class DownloadControlReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DownloadControlEntryPoint {
        fun downloadRepository(): DownloadRepository
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EXTRA_DOWNLOAD_ID) ?: return
        val repository = EntryPointAccessors
            .fromApplication(context.applicationContext, DownloadControlEntryPoint::class.java)
            .downloadRepository()

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_PAUSE -> repository.pause(id)
                    ACTION_CANCEL -> repository.cancel(id)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_PAUSE = "zechs.zplex.download.PAUSE"
        const val ACTION_CANCEL = "zechs.zplex.download.CANCEL"
        const val EXTRA_DOWNLOAD_ID = "download_id"
    }
}
