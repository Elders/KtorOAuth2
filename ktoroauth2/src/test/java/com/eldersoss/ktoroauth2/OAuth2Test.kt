package com.eldersoss.ktoroauth2

import com.eldersoss.ktoroauth2.authorizer.BasicAuthorizer
import com.eldersoss.ktoroauth2.authorizer.bearer
import com.eldersoss.ktoroauth2.flow.ClientCredentialsFlow
import com.eldersoss.ktoroauth2.flow.ResourceOwnerFlow
import com.eldersoss.ktoroauth2.throwable.InvalidGrandException
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Assert
import org.junit.Test
import java.nio.charset.StandardCharsets

class OAuth2Test {

    @Test
    fun testWithClientCredentialsFlow() = runBlocking {

        var requestAuthorization: String? = null

        var postBody: String? = null

        val engine = MockEngine { request ->

            val responseContent = when (request.url.toString()) {

                TOKEN_ENDPOINT -> {
                    postBody = (request.body as FormDataContent).bytes().toString(StandardCharsets.UTF_8)
                    """{
                            "access_token": "$VALID_TOKEN",
                            "expires_in": 3600,
                            "token_type": "Bearer"
                       }""".trimIndent()
                }

                API_ENDPOINT -> ""

                else -> throw NotImplementedError()
            }.toByteArray(StandardCharsets.UTF_8)

            requestAuthorization = request.headers[HttpHeaders.Authorization]

            respond(
                content = responseContent,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(engine) {

            install(OAuth2) {

                bearer(
                    ClientCredentialsFlow(
                        tokenEndPoint = TOKEN_ENDPOINT,
                        scope = "read",
                        authorizer = BasicAuthorizer(
                            "client", "secret"
                        )
                    )
                )
            }

            install(JsonFeature) {
                serializer = KotlinxSerializer(mockJson)
            }
        }

        client.get<String>(API_ENDPOINT)

        Assert.assertEquals("Bearer $VALID_TOKEN", requestAuthorization)
        Assert.assertEquals("grant_type=client_credentials&scope=read", postBody)
    }


    private val credentialsProvider: suspend (t: Throwable?) -> Credentials = {

        suspendCancellableCoroutine { continuation ->

            continuation.resumeWith(

                Result.success(

                    Credentials("username", "password")
                )
            )
        }
    }


    @Test
    fun testWithResourceOwnerFlow() = runBlocking {

        var requestAuthorization: String? = null

        var postBody: String? = null

        val engine = MockEngine { request ->

            val responseContent = when (request.url.toString()) {

                TOKEN_ENDPOINT -> {
                    postBody = (request.body as FormDataContent).bytes().toString(StandardCharsets.UTF_8)
                    """{
                            "access_token": "$VALID_TOKEN",
                            "expires_in": 3600,
                            "token_type": "Bearer",
                            "refresh_token": "$REFRESH_TOKEN"
                       }""".trimIndent()
                }

                API_ENDPOINT -> ""

                else -> throw NotImplementedError()
            }.toByteArray(StandardCharsets.UTF_8)

            requestAuthorization = request.headers[HttpHeaders.Authorization]

            respond(
                content = responseContent,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(engine) {

            install(OAuth2) {

                bearer(
                    ResourceOwnerFlow(
                        tokenEndPoint = TOKEN_ENDPOINT,
                        scope = "read",
                        authorizer = BasicAuthorizer(
                            "client", "secret"
                        ),
                        credentialsProvider = credentialsProvider
                    )
                )
            }

            install(JsonFeature) {
                serializer = KotlinxSerializer(mockJson)
            }
        }

        client.get<String>(API_ENDPOINT)

        Assert.assertEquals("Bearer $VALID_TOKEN", requestAuthorization)
        Assert.assertEquals("grant_type=password&username=username&password=password&scope=read", postBody)
    }

    @Test
    fun testInvalidGrantWithResourceOwnerFlow() = runBlocking {

        val engine = MockEngine { request ->

            when (request.url.toString()) {

                TOKEN_ENDPOINT -> {
                    val postBody = (request.body as FormDataContent).bytes().toString(StandardCharsets.UTF_8)
                    """{
                        	"error": "invalid_grant",
                        	"error_description": "Invalid username or password"
                       }""".trimIndent()

                    respond(
                        content = postBody.toByteArray(StandardCharsets.UTF_8),
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                else -> respond(
                    content = "",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }

        val client = HttpClient(engine) {

            install(OAuth2) {

                bearer(
                    ResourceOwnerFlow(
                        tokenEndPoint = TOKEN_ENDPOINT,
                        scope = "read",
                        authorizer = BasicAuthorizer(
                            "client", "secret"
                        ),
                        credentialsProvider = credentialsProvider
                    )
                )
            }

            install(JsonFeature) {
                serializer = KotlinxSerializer(mockJson)
            }
        }

        val exception: Throwable? =
            try {
                client.get<String>(API_ENDPOINT)
                null
            } catch (e: Throwable) {
                e
            }


        Assert.assertTrue(exception is InvalidGrandException)
        Assert.assertEquals("Invalid username or password", exception?.message)
    }
}