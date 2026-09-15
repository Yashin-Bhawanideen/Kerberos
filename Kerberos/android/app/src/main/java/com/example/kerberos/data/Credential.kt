package com.example.kerberos.data

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
