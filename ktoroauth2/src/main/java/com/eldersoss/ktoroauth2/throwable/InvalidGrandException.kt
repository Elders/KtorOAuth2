package com.eldersoss.ktoroauth2.throwable

class InvalidGrandException(message: String? = null) : OAuth2Exception(message ?: "The provided authorization grant or refresh token is invalid")