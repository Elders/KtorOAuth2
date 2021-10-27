package com.eldersoss.ktoroauth2

import com.eldersoss.ktoroauth2.authorizer.Authorizer
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.util.*
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
        }
    }
}
