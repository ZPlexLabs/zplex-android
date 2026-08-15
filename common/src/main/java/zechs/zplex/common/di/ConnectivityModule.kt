package zechs.zplex.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import zechs.zplex.common.connectivity.ConnectivityObserver
import zechs.zplex.common.connectivity.NetworkConnectivityObserver
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectivityModule {

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(
        impl: NetworkConnectivityObserver
    ): ConnectivityObserver
}
