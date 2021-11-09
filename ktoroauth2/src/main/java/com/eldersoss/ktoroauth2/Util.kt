package com.eldersoss.ktoroauth2

import java.nio.charset.StandardCharsets
import java.util.concurrent.CancellationException

private const val BASE64_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

// Because of mixed Base64 implementations in Java ans Android we cannot use native in the app and unit tests
internal fun String.base64encode(): String {

    val byteArray = this.toByteArray(StandardCharsets.UTF_8)

    val stringBuilder = StringBuilder()
    var pad = 0
    var index = 0

    while (index < byteArray.size) {

        var byte: Int = (byteArray[index].toInt() and 0xFF) shl 16 and 0xFFFFFF

        if (index + 1 < byteArray.size) {
            byte = byte or ((byteArray[index + 1].toInt() and 0xFF) shl 8)
        } else {
            pad++
        }

        if (index + 2 < byteArray.size) {
            byte = byte or (byteArray[index + 2].toInt() and 0xFF)
        } else {
            pad++
        }

        for (j in 0 until 4 - pad) {
            val charIndex = (byte and 0xFC0000) shr 18
            stringBuilder.append(BASE64_TABLE[charIndex])
            byte = byte shl 6
        }

        index += 3
    }

    for (j in 0 until pad) {
        stringBuilder.append("=")
    }

    return stringBuilder.toString()
}


internal fun Throwable.unwrapCancellationException(): Throwable {
    var exception: Throwable? = this
    while (exception is CancellationException) {
        // If there is a cycle, we return the initial exception.
        if (exception == exception.cause) {
            return this
        }
        exception = exception.cause
    }

    return exception ?: this
}
