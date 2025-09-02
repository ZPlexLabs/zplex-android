package zechs.zplex.utils.ext

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <T, K, R> LiveData<T>.combineWith(
    liveData: LiveData<K>,
    block: (T?, K?) -> R
) = MediatorLiveData<R>().also { mediator ->
    mediator.addSource(this) {
        mediator.value = block(this.value, liveData.value)
    }
    mediator.addSource(liveData) {
        mediator.value = block(this.value, liveData.value)
    }
}

fun <T> LiveData<T>.hasValue(): Boolean = this.value != null
