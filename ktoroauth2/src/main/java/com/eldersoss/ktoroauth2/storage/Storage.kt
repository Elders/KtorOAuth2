package com.eldersoss.ktoroauth2.storage

interface Storage {
    suspend fun read(key: String): String?
    suspend fun write(key: String, value: String)
    suspend fun delete(key: String)
}