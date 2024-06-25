package dev.gitlive.firebase.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.utf8
import platform.Foundation.NSCoder
import platform.Foundation.NSData
import platform.Foundation.NSSearchPathDirectory
import platform.Foundation.NSSearchPathDomainMask
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding

@OptIn(BetaInteropApi::class)
actual fun createTestData(): Data {
    val value = NSString.create(string = "test")
    return Data(value.dataUsingEncoding(NSUTF8StringEncoding, false)!!)
}