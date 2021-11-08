package com.eldersoss.ktoroauth2.authorizer

import com.eldersoss.ktoroauth2.KEY_BEARER
import com.eldersoss.ktoroauth2.OAuth2
import com.eldersoss.ktoroauth2.flow.AuthorizationFlow
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class BearerAuthorizer(private val authorizationFlow: AuthorizationFlow, private val requestBuilderBlock: (HttpRequestBuilder.() -> Unit)? = null) : Authorizer {

    override suspend fun authorize(request: HttpRequestBuilder, client: HttpClient) {
        // Only header authorization is supported

        val token = authorizationFlow.getToken(client)

        request.headers[HttpHeaders.Authorization] = "$KEY_BEARER ${token.accessToken}"

        requestBuilderBlock?.let {
            request.it()
        }
    }
}

fun OAuth2.bearer(authorizationFlow: AuthorizationFlow, requestBuilderBlock: (HttpRequestBuilder.() -> Unit)? = null) {

    this.authorizer = BearerAuthorizer(authorizationFlow)
}