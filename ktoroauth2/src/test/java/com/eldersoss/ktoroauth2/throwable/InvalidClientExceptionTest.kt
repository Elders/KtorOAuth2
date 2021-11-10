package com.eldersoss.ktoroauth2.throwable

import com.eldersoss.ktoroauth2.API_ENDPOINT
import com.eldersoss.ktoroauth2.OAuth2
import com.eldersoss.ktoroauth2.TOKEN_ENDPOINT
import com.eldersoss.ktoroauth2.authorizer.BasicAuthorizer
import com.eldersoss.ktoroauth2.authorizer.bearer
import com.eldersoss.ktoroauth2.flow.ClientCredentialsFlow
import com.eldersoss.ktoroauth2.mockJson
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class InvalidClientExceptionTest {

    @Test
    fun testInvalidGrantWithClientCredentialsFlow() = runBlocking {

        val engine = MockEngine { request ->
            respond(
                content = """{
                    "error": "invalid_client"
                }""".trimIndent(),
                status = HttpStatusCode.BadRequest,
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
                        ),
                    )
                )
            }

            install(JsonFeature) {
                serializer = KotlinxSerializer(mockJson)
            }
        }

        var exception: Throwable? = null

        try {
            client.get<String>(API_ENDPOINT)
        } catch (t: Throwable) {
            exception = t
        }

        Assert.assertTrue(exception is InvalidClientException)
    }
}