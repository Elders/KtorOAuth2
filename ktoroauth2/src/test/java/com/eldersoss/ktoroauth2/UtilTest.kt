package com.eldersoss.ktoroauth2

import org.junit.Assert
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.util.*

class UtilTest {

    @Test
    fun base64EncodeTest() {

        val someText = "username:password"

        Assert.assertEquals(
            Base64.getEncoder().encodeToString(someText.toByteArray(StandardCharsets.UTF_8)),
            someText.base64encode()
        )
    }

    @Test
    fun testWithDecoding() {

        val someText = "username:password"
        val encoded = someText.base64encode()

        Assert.assertEquals(someText, Base64.getDecoder().decode(encoded).toString(StandardCharsets.UTF_8))
    }

}