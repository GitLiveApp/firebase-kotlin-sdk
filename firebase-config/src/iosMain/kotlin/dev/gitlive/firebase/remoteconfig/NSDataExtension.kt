package dev.gitlive.firebase.remoteconfig

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.posix.memcpy

@ExperimentalUnsignedTypes
public fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    usePinned { memcpy(it.addressOf(0), bytes, length) }
}
