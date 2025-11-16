package zechs.zplex.utils.util

import okhttp3.Dns
import java.net.InetAddress
import java.net.UnknownHostException

class ChainDns(
    private val chain: List<Dns>
) : Dns {

    override fun lookup(hostname: String): List<InetAddress> {
        for (resolver in chain) {
            try {
                val result = resolver.lookup(hostname)
                if (result.isNotEmpty()) return result
            } catch (_: Exception) {
            }
        }
        throw UnknownHostException("All DNS resolvers in chain failed for $hostname")
    }
}