package com.eldersoss.ktoroauth2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Token(
    @SerialName(KEY_ACCESS_TOKEN) val accessToken: String,
    @SerialName(KEY_TOKEN_TYPE) val tokenType: String,
    @SerialName(KEY_EXPIRES_IN) val expiresIn: Long,
    @SerialName(KEY_REFRESH_TOKEN) val refreshToken: String? = null,
    val scope: String? = null
) {

    private val creationTime = System.currentTimeMillis()

    internal val isExpired: Boolean
        get() = System.currentTimeMillis() >= creationTime + (expiresIn * 1000)
}