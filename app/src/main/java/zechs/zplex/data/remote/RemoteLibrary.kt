package zechs.zplex.data.remote

interface RemoteLibrary {

    suspend fun indexMovies()
    suspend fun indexShows()

}