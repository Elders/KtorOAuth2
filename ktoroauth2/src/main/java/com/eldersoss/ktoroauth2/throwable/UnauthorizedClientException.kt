package com.eldersoss.ktoroauth2.throwable

class UnauthorizedClientException(message: String? = null) : OAuth2Exception(message ?: "The authenticated client is not authorized to use this authorization grant type")