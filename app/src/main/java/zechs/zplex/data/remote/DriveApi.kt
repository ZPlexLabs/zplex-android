package zechs.zplex.data.remote

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import zechs.zplex.data.model.drive.DriveResponse
import zechs.zplex.data.model.drive.FileResponse
import zechs.zplex.data.model.drive.FilesResponse

interface DriveApi {

    @GET("/drive/v3/files")
    suspend fun getFiles(
        @Header("Authorization")
        accessToken: String,
        @Query("supportsAllDrives")
        supportsAllDrives: Boolean = true,
        @Query("includeItemsFromAllDrives")
        includeItemsFromAllDrives: Boolean = true,
        @Query("pageSize")
        pageSize: Int = 25,
        @Query("pageToken")
        pageToken: String? = null,
        @Query("fields")
        fields: String = "nextPageToken, files(id, name, size, mimeType, iconLink, shortcutDetails, modifiedTime)",
        @Query("orderBy")
        orderBy: String = "folder, name",
        @Query("q")
        q: String
    ): FilesResponse

    @GET("/drive/v3/drives")
    suspend fun getDrives(
        @Header("Authorization")
        accessToken: String,
        @Query("pageSize")
        pageSize: Int = 25,
        @Query("pageToken")
        pageToken: String? = null,
        @Query("fields")
        fields: String = "nextPageToken, drives(id, name, kind)"
    ): DriveResponse

    @Streaming
    @GET("drive/v3/files/{fileId}?alt=media")
    suspend fun downloadFile(
        @Path("fileId") fileId: String,
        @Header("Authorization")
        accessToken: String
    ): ResponseBody

    @GET("drive/v3/files/{fileId}")
    suspend fun getFile(
        @Path("fileId") fileId: String,
        @Header("Authorization")
        accessToken: String,
        @Query("fields")
        fields: String = "id, name, size, modifiedTime, md5Checksum, mimeType"
    ): FileResponse
}