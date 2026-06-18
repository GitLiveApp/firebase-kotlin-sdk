package dev.gitlive.firebase.storage

import org.khronos.webgl.Uint8Array

actual fun createTestData(): Data = Data(stringToUint8Array("test"))

private fun stringToUint8Array(value: String): Uint8Array = js("new TextEncoder().encode(value)")
