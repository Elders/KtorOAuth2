package com.eldersoss.ktoroauth2.flow

import com.eldersoss.ktoroauth2.*
import com.eldersoss.ktoroauth2.authorizer.Authorizer
import io.ktor.client.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ClientCredentialsFlow(
    tokenEndPoint: String,
    scope: String,
    authorizer: Authorizer
) : AuthorizationFlow(tokenEndPoint, scope, authorizer) {


    private val mutex = Mutex()

    override suspend fun getToken(client: HttpClient): Token {

        mutex.withLock {

            validToken?.let {
                return it
            }

            val token = client.submitForm<Token>(url = tokenEndPoint,
                formParameters = Parameters.build {
                    append(KEY_GRANT_TYPE, KEY_CLIENT_CREDENTIALS)
                    append(KEY_SCOPE, scope)
                }) {

                this.attributes.put(OAuth2.authRequestAttribute, Unit)
                authorizer.authorize(this, client)
            }

            this.token = token

            return token
        }
    }
}