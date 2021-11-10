package com.eldersoss.ktoroauth2.throwable

import com.eldersoss.ktoroauth2.*
import com.eldersoss.ktoroauth2.authorizer.BasicAuthorizer
import com.eldersoss.ktoroauth2.authorizer.bearer
import com.eldersoss.ktoroauth2.flow.ResourceOwnerFlow
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.nio.charset.StandardCharsets

class InvalidGrantExceptionTest {

    @Test
    fun testInvalidGrant() = runBlocking {

        var step = 0

        val throwableList = ArrayList<Throwable?>()

        val engine = MockEngine { request ->

            when (request.url.toString()) {

                TOKEN_ENDPOINT -> {


                    val postBody = (request.body as FormDataContent).bytes().toString(StandardCharsets.UTF_8)

                    if (postBody.contains("WRONGpassword")) {
                        respond(
                            content = """{
                                    "error": "invalid_grant",
                                    "error_description": "Invalid username or password"
                                }""".trimIndent(),
                            status = HttpStatusCode.BadRequest,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    } else {
                        respond(
                            content = """{
                            "access_token": "$VALID_TOKEN",
                            "expires_in": 3600,
                            "token_type": "Bearer",
                            "refresh_token": "$REFRESH_TOKEN"
                       }""".trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                }

                else -> respond(
                    content = "SUCCESS",
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
                        credentialsProvider = {
                            throwableList.add(it)

                            when (step++) {
                                0 -> Credentials("username", "WRONGpassword")
                                else -> Credentials("username", "password")
                            }
                        }
                    )
                )
            }

            install(JsonFeature) {
                serializer = KotlinxSerializer(mockJson)
            }
        }

        val result = client.get<String>(API_ENDPOINT)

        Assert.assertEquals("SUCCESS", result)

        Assert.assertEquals(2, throwableList.size)
        Assert.assertNull(throwableList[0])
        Assert.assertTrue(throwableList[1] is InvalidGrandException)
        Assert.assertEquals("Invalid username or password", throwableList[1]?.message)
    }
}