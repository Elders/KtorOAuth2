package com.eldersoss.ktoroauth2

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

val dummyClient = HttpClient(MockEngine {
    respond(
        content = "",
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
})

val mockJson = Json { ignoreUnknownKeys = true }

const val VALID_TOKEN = "fvQK3HPmgAWXCB6NhkWe3njZr7MUaTmvVBfdCVAg"

const val VALID_TOKEN2 = "exUwTYxhcdEXsLsA3fAirxYCuWwmQjZDd84aGz8Y"

const val INVALID_TOKEN = "fUzPWNRvYzUZhe42e3diXp8oRCi63DVDXwDKLrya"

const val REFRESH_TOKEN = "g7NQGW8Nu9U2"

const val EXPIRED_REFRESH_TOKEN = "v8fwTyb3JnMh"

const val TOKEN_ENDPOINT = "https://iaa.com/token"

const val API_ENDPOINT = "https://api.com/user"