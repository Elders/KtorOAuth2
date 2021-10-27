package com.eldersoss.ktoroauth2.flow

import com.eldersoss.ktoroauth2.*
import com.eldersoss.ktoroauth2.authorizer.BasicAuthorizer
import com.eldersoss.ktoroauth2.storage.Storage
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.nio.charset.StandardCharsets

class ResourceOwnerFlowTest {

    @Test
    fun testGetTokenAndRefreshToken() = runBlocking {

        var step = 0

        var postBody: String? = null

        val engine = MockEngine { request ->

            postBody = (request.body as FormDataContent).bytes().toString(StandardCharsets.UTF_8)

            val responseContent = when (step) {

                0 -> {
                    """{
                            "access_token": "$VALID_TOKEN",
                            "expires_in": 1,
                            "token_type": "Bearer",
                            "refresh_token": "$REFRESH_TOKEN"
                       }""".trimIndent()
                }

                1 -> {
                    """{
                            "access_token": "$VALID_TOKEN2",
                            "expires_in": 3600,
                            "token_type": "Bearer",
                            "refresh_token": "$REFRESH_TOKEN"
                       }""".trimIndent()
                }


                else -> throw NotImplementedError()
            }.toByteArray(StandardCharsets.UTF_8)

            respond(
                content = responseContent,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(engine) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(mockJson)
            }
        }

        val flow = ResourceOwnerFlow(TOKEN_ENDPOINT, "read", BasicAuthorizer("client", "secret")) { Credentials("username", "password") }

        var token = flow.getToken(client)

        Assert.assertEquals(VALID_TOKEN, token.accessToken)

        Assert.assertEquals("grant_type=password&username=username&password=password&scope=read", postBody)

        delay(1000)
        step++

        token = flow.getToken(client)

        Assert.assertEquals(VALID_TOKEN2, token.accessToken)

        Assert.assertEquals("grant_type=refresh_token&refresh_token=$REFRESH_TOKEN", postBody)
    }


    @Test
    fun testRefreshTokenStoringAndUsing() = runBlocking {

        var postBody: String? = null

        val storage = object : Storage {

            val map = HashMap<String, String>()

            override suspend fun read(key: String): String? {
                return map[key]
            }

            override suspend fun delete(key: String) {
                map.remove(key)
            }

            override suspend fun write(key: String, value: String) {
                map[key] = value
            }
        }

        val engine = MockEngine { request ->

            postBody = (request.body as FormDataContent).bytes().toString(StandardCharsets.UTF_8)

            val responseContent =
                """{
                        "access_token": "$VALID_TOKEN",
                        "expires_in": 3600,
                        "token_type": "Bearer",
                        "refresh_token": "$REFRESH_TOKEN"
                   }""".trimIndent().toByteArray(StandardCharsets.UTF_8)

            respond(
                content = responseContent,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(engine) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(mockJson)
            }
        }

        var flow = ResourceOwnerFlow(TOKEN_ENDPOINT, "read", storage = storage, authorizer = BasicAuthorizer("client", "secret")) { Credentials("username", "password") }

        var token = flow.getToken(client)

        Assert.assertEquals(VALID_TOKEN, token.accessToken)

        Assert.assertEquals("grant_type=password&username=username&password=password&scope=read", postBody)

        // New instance with the same storage should use refresh token

        flow = ResourceOwnerFlow(TOKEN_ENDPOINT, "read", storage = storage, authorizer = BasicAuthorizer("client", "secret")) { Credentials("username", "password") }

        token = flow.getToken(client)

        Assert.assertEquals(VALID_TOKEN, token.accessToken)

        Assert.assertEquals("grant_type=refresh_token&refresh_token=$REFRESH_TOKEN", postBody)
    }
}