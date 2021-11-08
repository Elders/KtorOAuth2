package com.eldersoss.ktoroauth2.throwable

class InvalidRequestException(message: String? = null) : OAuth2Exception(message ?: "The request is missing a required parameter")