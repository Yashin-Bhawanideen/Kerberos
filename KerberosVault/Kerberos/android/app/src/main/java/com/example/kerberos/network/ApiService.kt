package com.example.kerberos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

// Retrofit interface declaring the REST endpoints for credential CRUD.
// Retrofit generates the implementation at runtime based on these annotations.
interface ApiService {

    // Fetches all credentials for the current user
    @GET("api/credentials")
    suspend fun getCredentials(): Response<List<CredentialDto>>

    // Fetches a single credential by ID
    @GET("api/credentials/{id}")
    suspend fun getCredential(@Path("id") id: String): Response<CredentialDto>

    // Creates a new credential; server assigns id/timestamps and returns
    // the full created object
    @POST("api/credentials")
    suspend fun addCredential(@Body request: CreateCredentialRequest): Response<CredentialDto>

    //updates an existing credential through the REST API (Square, 2026)

    @PUT("api/credentials/{id}")
    suspend fun updateCredential(
        @Path("id") id: String,
        @Body request: UpdateCredentialRequest
    ): Response<CredentialDto>

    // Deletes a credential by ID; no response body expected on success
    @DELETE("api/credentials/{id}")
    suspend fun deleteCredential(@Path("id") id: String): Response<Unit>
}
//References
//Developers, A., 2026. Android API reference. [Online]
//Available at: https://developer.android.com/reference
//Geek4geeks, 2025. Networking and API Integration in Android. [Online]
//Available at: https://www.geeksforgeeks.org/kotlin/networking-and-api-integration-in-android/

//Square, 2026. Retrofit. [Online]
//Available at: https://square.github.io/retrofit/
//[Accessed 22 September 2026].