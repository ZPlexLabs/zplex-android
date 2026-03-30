package zechs.zplex.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import zechs.zplex.common.utils.Result
import zechs.zplex.zplex_api.data.local.PaginatedResponse

class Pager<T>(
    private val scope: CoroutineScope,
    private val request: suspend (page: Int) -> Result<PaginatedResponse<T>>
) {

    private val _state = MutableStateFlow<UiResult<T>>(UiResult.Idle())
    val state: StateFlow<UiResult<T>> = _state.asStateFlow()

    private var currentPage = 1
    private var totalPages = Int.MAX_VALUE
    private val accumulated = mutableListOf<T>()

    fun loadNext(reset: Boolean = false) {
        val currentState = _state.value

        if (currentState is UiResult.Loading) return

        if (reset) {
            currentPage = 1
            totalPages = Int.MAX_VALUE
            accumulated.clear()
            _state.value = UiResult.Idle()
        }

        if (currentPage > totalPages) return

        scope.launch(Dispatchers.IO) {

            _state.value = UiResult.Loading(accumulated.toList())

            when (val result = request(currentPage)) {

                is Result.Success -> {
                    val response = result.data

                    accumulated.addAll(response.data)

                    currentPage = response.pageNumber + 1
                    totalPages = response.pageCount

                    _state.value = UiResult.Success(
                        data = accumulated.toList(),
                        endReached = currentPage > totalPages
                    )
                }

                is Result.Error -> {
                    _state.value = UiResult.Error(
                        message = result.message,
                        data = accumulated.toList()
                    )
                }
            }
        }
    }

    fun retry() {
        loadNext(reset = false)
    }

    fun refresh() {
        loadNext(reset = true)
    }
}