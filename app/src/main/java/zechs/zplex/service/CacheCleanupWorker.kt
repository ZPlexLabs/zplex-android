package zechs.zplex.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import zechs.zplex.data.local.api_cache.ApiCacheDao
import javax.inject.Inject

class CacheCleanupWorkerFactory @Inject constructor(
    private val apiCacheDao: ApiCacheDao
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = CacheCleanupWorker(appContext, workerParameters, apiCacheDao)
}

@HiltWorker
class CacheCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apiCacheDao: ApiCacheDao
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "CacheCleanupWorker"
    }

    override suspend fun doWork(): Result {
        val currentTime = System.currentTimeMillis()
        try {
            apiCacheDao.deleteExpiredCache(currentTime)
            Log.d(TAG, "Cache cleanup successful.")
        } catch (e: Exception) {
            Log.d(TAG, "Cache cleanup failed.")
            return Result.failure()
        }
        return Result.success()
    }

}
