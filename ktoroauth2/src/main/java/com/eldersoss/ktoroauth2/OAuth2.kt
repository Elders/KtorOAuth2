package com.eldersoss.ktoroauth2

import com.eldersoss.ktoroauth2.authorizer.Authorizer
import com.eldersoss.ktoroauth2.throwable.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.core.*


class OAuth2 {

    internal var authorizer: Authorizer? = null

    companion object Feature : HttpClientFeature<OAuth2, OAuth2> {

        val authRequestAttribute = AttributeKey<Unit>("AuthenticationRequest")

        override val key: AttributeKey<OAuth2> = AttributeKey("DigestAuth")

        override fun prepare(block: OAuth2.() -> Unit): OAuth2 {
            return OAuth2().apply(block)
        }

        override fun install(feature: OAuth2, scope: HttpClient) {

            scope.requestPipeline.intercept(HttpRequestPipeline.State) {

                if (this.context.attributes.contains(authRequestAttribute)) return@intercept

                feature.authorizer?.authorize(this.context, scope)
            }

            scope.requestPipeline.intercept(HttpRequestPipeline.Before) {
                try {
                    proceedWith(it)
                } catch (cause: Throwable) {
                    try {
                        (cause as ResponseException).response.receive<OAuth2Error>().getException()
                    } catch (t: Throwable) {
                        null
                    }?.let { oAuth2Exception ->
                        throw oAuth2Exception
                    }

                    val unwrappedCause = cause.unwrapCancellationException()
                    throw unwrappedCause
                }
            }
        }
    }
}
