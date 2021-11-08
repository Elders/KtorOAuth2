package com.eldersoss.ktoroauth2.throwable

class InvalidClientException(message: String? = null) : OAuth2Exception(message ?: "Unknown client, no client authentication included, or unsupported authentication method")