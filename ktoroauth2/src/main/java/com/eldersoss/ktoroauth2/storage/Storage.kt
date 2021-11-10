package com.eldersoss.ktoroauth2.storage

interface Storage {
    suspend fun read(): String?
    suspend fun write(value: String)
    suspend fun delete()
}