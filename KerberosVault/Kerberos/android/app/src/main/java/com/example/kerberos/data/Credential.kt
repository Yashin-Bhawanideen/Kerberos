package com.example.kerberos.data

// Plain domain/UI model for a credential (as opposed to CredentialEntity,
// which is the Room-persisted representation with syncState included).
// Used for passing data between UI, repository and network layers.
data class Credential(
    val id: String = "",
    val serviceName: String = "",
    val username: String = "",
    val password: String = "",
    val websiteUrl: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)
