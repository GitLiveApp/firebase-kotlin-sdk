package dev.gitlive.firebase.storage

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding

@OptIn(BetaInteropApi::class)
actual fun createTestData(): Data {
    val value = NSString.create(string = "test")
    return Data(value.dataUsingEncoding(NSUTF8StringEncoding, false)!!)
}
