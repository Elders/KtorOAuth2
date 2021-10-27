package com.eldersoss.ktoroauth2.authorizer

import io.ktor.client.*
import io.ktor.client.request.*

interface Authorizer {

    suspend fun authorize(request: HttpRequestBuilder, client: HttpClient)
}