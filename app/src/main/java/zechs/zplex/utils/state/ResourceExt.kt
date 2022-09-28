package zechs.zplex.utils.state

import java.io.IOException

class ResourceExt {

    companion object {

        fun <T> postError(exception: Exception): Resource<T> {
            return Resource.Error(
                message = if (exception is IOException) {
                    "Network Failure"
                } else exception.message ?: "Something went wrong",
                data = null
            )
        }

    }

}
