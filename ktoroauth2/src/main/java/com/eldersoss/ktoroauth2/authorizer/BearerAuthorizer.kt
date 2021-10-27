package com.eldersoss.ktoroauth2.authorizer

import com.eldersoss.ktoroauth2.authorizer.Authorizer
import com.eldersoss.ktoroauth2.KEY_BEARER
import com.eldersoss.ktoroauth2.OAuth2
import com.eldersoss.ktoroauth2.flow.AuthorizationFlow
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class BearerAuthorizer(private val authorizationFlow: AuthorizationFlow) : Authorizer {

    override suspend fun authorize(request: HttpRequestBuilder, client: HttpClient) {
        // Only header authorization is supported

        val token = authorizationFlow.getToken(client)

        request.headers[HttpHeaders.Authorization] = "$KEY_BEARER ${token.accessToken}"
    }
}

fun OAuth2.bearer(authorizationFlow: AuthorizationFlow,) {

    this.authorizer = BearerAuthorizer(authorizationFlow)
}