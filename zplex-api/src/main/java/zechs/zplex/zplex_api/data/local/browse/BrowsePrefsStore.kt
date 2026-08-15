package zechs.zplex.zplex_api.data.local.browse

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import zechs.zplex.zplex_api.data.browse.FilterQuery
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.enums.OrderBy
import zechs.zplex.zplex_api.data.remote.api.enums.SortBy
import javax.inject.Inject
import javax.inject.Singleton

private val Context.browseDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "zplex_browse"
)

data class BrowsePreferences(
    val sortBy: SortBy = SortBy.DATE_ADDED,
    val orderBy: OrderBy = OrderBy.DESC,
    val filter: FilterQuery = FilterQuery()
)

/** Persists the sort/order/filter selection per [MediaType] browse screen. */
@Singleton
class BrowsePrefsStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    moshi: Moshi
) {
    private val filterAdapter = moshi.adapter(FilterQuery::class.java)

    fun preferences(mediaType: MediaType): Flow<BrowsePreferences> =
        context.browseDataStore.data.map { prefs ->
            BrowsePreferences(
                sortBy = prefs[sortKey(mediaType)]?.let { runCatching { SortBy.valueOf(it) }.getOrNull() }
                    ?: SortBy.DATE_ADDED,
                orderBy = prefs[orderKey(mediaType)]?.let { runCatching { OrderBy.valueOf(it) }.getOrNull() }
                    ?: OrderBy.DESC,
                filter = prefs[filterKey(mediaType)]
                    ?.let { runCatching { filterAdapter.fromJson(it) }.getOrNull() }
                    ?: FilterQuery()
            )
        }

    suspend fun save(mediaType: MediaType, preferences: BrowsePreferences) {
        context.browseDataStore.edit { prefs ->
            prefs[sortKey(mediaType)] = preferences.sortBy.name
            prefs[orderKey(mediaType)] = preferences.orderBy.name
            prefs[filterKey(mediaType)] = filterAdapter.toJson(preferences.filter)
        }
    }

    private fun sortKey(type: MediaType) = stringPreferencesKey("sort_${type.name}")
    private fun orderKey(type: MediaType) = stringPreferencesKey("order_${type.name}")
    private fun filterKey(type: MediaType) = stringPreferencesKey("filter_${type.name}")
}
