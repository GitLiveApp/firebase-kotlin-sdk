package dev.gitlive.firebase.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.isEqualToData
import kotlin.test.assertTrue

@OptIn(BetaInteropApi::class)
actual fun createTestData(): Data {
    val value = NSString.create(string = "test")
    return Data(value.dataUsingEncoding(NSUTF8StringEncoding, false)!!)
}

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
actual fun assertTestDataEquals(data: Data) {
    val value = NSString.create(string = "test")
    val expectedData = value.dataUsingEncoding(NSUTF8StringEncoding, false)!!
    assertTrue(data.data.isEqualToData(expectedData as NSData))
}
