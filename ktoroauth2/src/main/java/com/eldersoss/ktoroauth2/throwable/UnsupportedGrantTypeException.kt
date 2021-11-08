package com.eldersoss.ktoroauth2.throwable

class UnsupportedGrantTypeException(message: String? = null) : OAuth2Exception(message ?: "The authorization grant type is not supported by the authorization server")