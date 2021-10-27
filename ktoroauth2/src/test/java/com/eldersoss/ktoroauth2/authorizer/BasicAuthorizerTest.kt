package com.eldersoss.ktoroauth2.authorizer

import com.eldersoss.ktoroauth2.base64encode
import com.eldersoss.ktoroauth2.dummyClient
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class BasicAuthorizerTest {

    @Test
    fun authorizationTest() = runBlocking {

        val requestBuilder = HttpRequestBuilder()

        val basicAuthorizer = BasicAuthorizer("username", "password")

        basicAuthorizer.authorize(requestBuilder, dummyClient)

        val requestData = requestBuilder.build()

        Assert.assertEquals("Basic ${"username:password".base64encode()}", requestData.headers[HttpHeaders.Authorization])
    }
}