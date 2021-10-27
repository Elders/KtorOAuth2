package com.eldersoss.ktoroauth2.flow

import com.eldersoss.ktoroauth2.Token
import com.eldersoss.ktoroauth2.authorizer.Authorizer
import io.ktor.client.*

abstract class AuthorizationFlow(
    protected val tokenEndPoint: String,
    protected val scope: String,
    protected val authorizer: Authorizer
) {

    protected var token: Token? = null

    protected val validToken: Token?
        get() {
            if (this.token?.isExpired == false) {
                return token
            }
            return null
        }

    abstract suspend fun getToken(client: HttpClient): Token
}