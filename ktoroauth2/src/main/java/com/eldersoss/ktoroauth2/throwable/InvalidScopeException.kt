package com.eldersoss.ktoroauth2.throwable

class InvalidScopeException(message: String? = null) : OAuth2Exception(message ?: "The requested scope is invalid")