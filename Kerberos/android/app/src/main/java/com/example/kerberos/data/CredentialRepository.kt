package com.example.kerberos.data

import com.example.kerberos.network.CreateCredentialRequest
import com.example.kerberos.network.CredentialDto
import com.example.kerberos.network.RetrofitClient

class CredentialRepository {

    private val api = RetrofitClient.apiService

    private fun CredentialDto.toCredential() = Credential(
        id = id, serviceName = serviceName, username = username,
        password = password, websiteUrl = websiteUrl, notes = notes,
        createdAt = createdAt, modifiedAt = modifiedAt
    )

    suspend fun addCredential(credential: Credential): Result<Unit> {
        return try {
            val request = CreateCredentialRequest(
                serviceName = credential.serviceName,
                username = credential.username,
                password = credential.password,
                websiteUrl = credential.websiteUrl,
                notes = credential.notes
            )
            val response = api.addCredential(request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("API error: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCredentials(): Result<List<Credential>> {
        return try {
            val response = api.getCredentials()
            if (response.isSuccessful) {
                Result.success(response.body()?.map { it.toCredential() } ?: emptyList())
            } else Result.failure(Exception("API error: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCredential(id: String): Result<Unit> {
        return try {
            val response = api.deleteCredential(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("API error: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
