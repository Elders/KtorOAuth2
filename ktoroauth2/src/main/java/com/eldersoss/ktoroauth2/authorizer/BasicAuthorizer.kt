package com.eldersoss.ktoroauth2.authorizer

import com.eldersoss.ktoroauth2.KEY_BASIC
import com.eldersoss.ktoroauth2.base64encode
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class BasicAuthorizer(private val userName: String, private val password: String) : Authorizer {

    override suspend fun authorize(request: HttpRequestBuilder, client: HttpClient) {

        val authBase64 = "${userName}:${password}".base64encode()

        request.headers[HttpHeaders.Authorization] = "$KEY_BASIC $authBase64"
    }
}