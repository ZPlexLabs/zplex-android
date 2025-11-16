package zechs.zplex.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import zechs.zplex.utils.util.ChainDns
import java.net.InetAddress
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LowLevelNetwork {

    @Provides
    @Singleton
    @Named("BootstrapClient")
    fun provideBootstrapClient(): OkHttpClient =
        OkHttpClient.Builder()
            .build()

    @Provides
    @Singleton
    @Named("cloudflare")
    fun provideCloudflareDoH(@Named("BootstrapClient") bootstrapClient: OkHttpClient): Dns {
        val dohUrl = "https://cloudflare-dns.com/dns-query".toHttpUrl()
        return DnsOverHttps.Builder()
            .client(bootstrapClient)
            .url(dohUrl)
            .bootstrapDnsHosts(
                InetAddress.getByName("1.1.1.1"),
                InetAddress.getByName("1.0.0.1")
            )
            .build()
    }

    @Provides
    @Singleton
    @Named("google")
    fun provideGoogleDoH(@Named("BootstrapClient") bootstrapClient: OkHttpClient): Dns {
        val dohUrl = "https://dns.google/dns-query".toHttpUrl()
        return DnsOverHttps.Builder()
            .client(bootstrapClient)
            .url(dohUrl)
            .bootstrapDnsHosts(
                InetAddress.getByName("8.8.8.8"),
                InetAddress.getByName("8.8.4.4")
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideChainDns(
        @Named("cloudflare") cloudflareDoH: Dns,
        @Named("google") googleDoH: Dns
    ): Dns = ChainDns(listOf(cloudflareDoH, googleDoH, Dns.SYSTEM))
}