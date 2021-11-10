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
    private val retryAuthentication: Boolean = true,
    private val credentialsProvider: suspend (t: Throwable?) -> Credentials
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
                        storage?.write(refreshToken)
                    }
                    return token
                }
            } catch (t: Throwable) {
                // just continue requesting credentials
            }

            val token = authenticate(client, null)

            this.token = token
            this.token?.refreshToken?.let { refreshToken ->
                storage?.write(refreshToken)
            }

            return token
        }
    }

    suspend fun revokeAuthentication() {

        this.token = null
        this.storage?.delete()
    }

    private suspend fun authenticate(client: HttpClient, t: Throwable?): Token {
        return try {
            tryAuthenticate(client, t)
        } catch (t: Throwable) {
            if (retryAuthentication) {
                authenticate(client, t)
            } else {
                throw t
            }
        }
    }

    private suspend fun tryAuthenticate(client: HttpClient, t: Throwable?): Token {

        val credentials: Credentials = credentialsProvider.invoke(t)

        return client.submitForm(url = tokenEndPoint,
            formParameters = Parameters.build {
                append(KEY_GRANT_TYPE, KEY_PASSWORD)
                append(KEY_USERNAME, credentials.username)
                append(KEY_PASSWORD, credentials.password)
                append(KEY_SCOPE, scope)
            }) {

            this.attributes.put(OAuth2.authRequestAttribute, Unit)
            authorizer.authorize(this, client)
        }
    }

    private suspend fun tryRefreshToken(client: HttpClient): Token? {

        token?.refreshToken?.let {

            return refreshToken(it, client)
        }

        storage?.read()?.let {

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
}