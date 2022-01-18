package dev.gitlive.firebase.remoteconfig

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.posix.memcpy

@ExperimentalUnsignedTypes
fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        usePinned { memcpy(it.addressOf(0), bytes, length) }
    }
}
