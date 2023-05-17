package dev.gitlive.firebase.remoteconfig

import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class NSDataExtensionTest {
    @Test
    fun testNSDataToByteArray() {
        val nsData = NSString
            .create(string = "Hello world")
            .dataUsingEncoding(NSUTF8StringEncoding)!!
        assertEquals("Hello world", nsData.toByteArray().decodeToString())
    }
}
