package com.eldersoss.ktoroauth2.flow

import com.eldersoss.ktoroauth2.*
import com.eldersoss.ktoroauth2.authorizer.Authorizer
import com.eldersoss.ktoroauth2.storage.Storage
import io.ktor.client.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ResourceOwnerFlow(
    tokenEndPoint: String,
    scope: String,
    authorizer: Authorizer,
    private val storage: Storage? = null,
    private val credentials: suspend () -> Credentials
) : AuthorizationFlow(tokenEndPoint, scope, authorizer) {

    private val mutex = Mutex()

    override suspend fun getToken(client: HttpClient): Token {

        mutex.withLock {

            validToken?.let {
                return it
            }

            try {
                tryRefreshToken(client)?.let { token ->

                    this.token = token
                    this.token?.refreshToken?.let { refreshToken ->
                        storage?.write(KEY_STORED_REFRESH_TOKEN, refreshToken)
                    }
                    return token
                }
            } catch (t: Throwable) {
                // just continue requesting credentials
            }

            val credentials: Credentials = credentials.invoke()

            val token = client.submitForm<Token>(url = tokenEndPoint,
                formParameters = Parameters.build {
                    append(KEY_GRANT_TYPE, KEY_PASSWORD)
                    append(KEY_USERNAME, credentials.username)
                    append(KEY_PASSWORD, credentials.password)
                    append(KEY_SCOPE, scope)
                }) {

                this.attributes.put(OAuth2.authRequestAttribute, Unit)
                authorizer.authorize(this, client)
            }

            this.token = token
            this.token?.refreshToken?.let { refreshToken ->
                storage?.write(KEY_STORED_REFRESH_TOKEN, refreshToken)
            }
            return token
        }
    }

    suspend fun revokeAuthentication() {

        this.token = null
        this.storage?.delete(KEY_STORED_REFRESH_TOKEN)
    }

    private suspend fun tryRefreshToken(client: HttpClient): Token? {

        token?.refreshToken?.let {

            return refreshToken(it, client)
        }

        storage?.read(KEY_STORED_REFRESH_TOKEN)?.let {

            return refreshToken(it, client)
        }

        return null
    }

    private suspend fun refreshToken(refreshToken: String, client: HttpClient): Token {
        return client.submitForm(
            url = tokenEndPoint,
            formParameters = Parameters.build {
                append(KEY_GRANT_TYPE, KEY_REFRESH_TOKEN)
                append(KEY_REFRESH_TOKEN, refreshToken)
            }) {

            this.attributes.put(OAuth2.authRequestAttribute, Unit)
            authorizer.authorize(this, client)
        }
    }

    companion object {

        private const val KEY_STORED_REFRESH_TOKEN = "mrgYKidHtJB7"
    }
}