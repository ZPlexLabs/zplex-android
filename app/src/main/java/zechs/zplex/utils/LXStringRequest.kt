package zechs.zplex.utils

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import java.nio.charset.StandardCharsets

class LXStringRequest(
    method: Int,
    url: String?,
    listener: Response.Listener<String?>?,
    errorListener: Response.ErrorListener?
) : StringRequest(method, url, listener, errorListener) {
    override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
        return Response.success(
            String(response.data, StandardCharsets.UTF_8),
            HttpHeaderParser.parseCacheHeaders(response)
        )
    }
}