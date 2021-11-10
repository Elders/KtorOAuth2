package com.eldersoss.ktoroauth2.throwable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
internal data class OAuth2Error(
    val error: String,
    @SerialName("error_description")
    val description: String? = null
) {

    fun getException(): OAuth2Exception? {
        return when (this.error) {
            "invalid_request" -> InvalidRequestException(this.description)
            "invalid_client" -> InvalidClientException(this.description)
            "invalid_grant" -> InvalidGrandException(this.description)
            "unauthorized_client" -> UnauthorizedClientException(this.description)
            "unsupported_grant_type" -> UnsupportedGrantTypeException(this.description)
            "invalid_scope" -> InvalidScopeException(this.description)
            else -> null
        }
    }
}