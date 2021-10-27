package com.eldersoss.ktoroauth2.authorizer

import com.eldersoss.ktoroauth2.*
import com.eldersoss.ktoroauth2.flow.AuthorizationFlow
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class BearerAuthorizerTest {

    @Test
    fun authorizationTest() = runBlocking {

        val requestBuilder = HttpRequestBuilder()

        val bearerAuthorizer = BearerAuthorizer(object : AuthorizationFlow(authorizer = object : Authorizer {
            override suspend fun authorize(request: HttpRequestBuilder, client: HttpClient) {

            }
        }, tokenEndPoint = TOKEN_ENDPOINT, scope = "read") {

            override suspend fun getToken(client: HttpClient): Token {
                return Token(VALID_TOKEN, "Bearer", 3600, REFRESH_TOKEN)
            }
        })

        bearerAuthorizer.authorize(requestBuilder, dummyClient)

        val requestData = requestBuilder.build()

        Assert.assertEquals("Bearer $VALID_TOKEN", requestData.headers[HttpHeaders.Authorization])
    }
}