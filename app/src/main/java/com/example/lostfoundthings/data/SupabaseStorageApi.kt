package com.example.lostfoundthings.data

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SupabaseStorageApi {
    @POST("storage/v1/object/{bucket}/{path}")
    suspend fun uploadFile(
        @Header("Authorization") apiKey: String,
        @Header("apikey") apikeyHeader: String,
        @Path("bucket") bucket: String,
        @Path("path") path: String,
        @Body file: RequestBody
    ): Response<Unit>
}