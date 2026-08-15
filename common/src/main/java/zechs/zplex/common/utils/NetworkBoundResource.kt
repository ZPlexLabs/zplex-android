package zechs.zplex.common.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Stale-while-revalidate: emits the cached value immediately, then revalidates from the
 * network when [shouldFetch] allows, persisting fresh data so the cache flow re-emits.
 */
inline fun <DB, REMOTE> networkBoundResource(
    crossinline query: () -> Flow<DB>,
    crossinline fetch: suspend () -> Result<REMOTE>,
    crossinline saveFetchResult: suspend (REMOTE) -> Unit,
    crossinline shouldFetch: (DB) -> Boolean = { true }
): Flow<CacheResource<DB>> = flow {
    val cached = query().first()
    val stream = if (shouldFetch(cached)) {
        emit(CacheResource.Loading(cached))
        when (val result = fetch()) {
            is Result.Success -> {
                saveFetchResult(result.data)
                query().map { CacheResource.Success(it) }
            }

            is Result.Error -> query().map {
                CacheResource.Error(result.message, result.details, it)
            }
        }
    } else {
        query().map { CacheResource.Success(it) }
    }
    emitAll(stream)
}
