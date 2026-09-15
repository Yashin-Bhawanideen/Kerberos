package com.example.kerberos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("api/credentials")
    suspend fun getCredentials(): Response<List<CredentialDto>>

    @GET("api/credentials/{id}")
    suspend fun getCredential(@Path("id") id: String): Response<CredentialDto>

    @POST("api/credentials")
    suspend fun addCredential(@Body request: CreateCredentialRequest): Response<CredentialDto>

    @DELETE("api/credentials/{id}")
    suspend fun deleteCredential(@Path("id") id: String): Response<Unit>
}
