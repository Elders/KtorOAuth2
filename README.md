#  KtorOAuth2

[![](https://jitpack.io/v/Elders/KtorOAuth2.svg)](https://jitpack.io/#Elders/KtorOAuth2)

This is extension library for Ktor Android client which provides OAuth2 implementation.


Supported flows:
- Client Credentials Flow
- Resource Owner Flow

## Installation
Add JitPack repository to your project build.gradle or settings.gradle file
```groovy
    maven { url 'https://jitpack.io' }
```
then include the library
```groovy
    implementation "com.github.Elders:KtorOAuth2:$ver"
```

## Usage
```kotlin
val credentialsProvider: suspend (t: Throwable?) -> Credentials = { throwable ->
    /**
     * A little bit weird this function has throwable parameter,
     * but in order to retry authentication process without exiting the request execution,
     * internally we handle all exceptions caused by authentication requests
     */
    throwable?.let {
        // TODO: Show exception message to the user or handle this exception if you want.
    }
    // This is suspend function, we can block it at this point while the user provides his credentials
    Credentials("username", "password")
}

// Flow configuration is pretty simple
val flow = ResourceOwnerFlow(
    tokenEndPoint = TOKEN_ENDPOINT,
    scope = "read",
    authorizer = BasicAuthorizer(CLIENT, SECRET),
    storage = refreshTokenStorage, // Implement the Storage interface
    credentialsProvider = credentialsProvider
)

// Configure Ktor as you want just install the OAuth2 feature
val client = HttpClient(engine) {
    install(OAuth2) {
        bearer(flow)
    }
}

// DONE: all the client requests will be authorized
```
