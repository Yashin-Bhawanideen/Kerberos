package com.example.kerberos.network
// Wire-format representation of a credential as returned by the API.
data class CredentialDto(
    val id: String = "",
    val serviceName: String = "",
    val username: String = "",
    val password: String = "",
    val websiteUrl: String = "",
    val notes: String = "",
    val createdAt: Long = 0L,
    val modifiedAt: Long = 0L
)

data class CreateCredentialRequest(
    val serviceName: String,
    val username: String,
    val password: String,
    val websiteUrl: String,
    val notes: String
)
