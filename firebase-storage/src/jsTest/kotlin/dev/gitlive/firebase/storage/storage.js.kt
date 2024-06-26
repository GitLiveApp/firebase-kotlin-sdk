package dev.gitlive.firebase.storage

import org.khronos.webgl.Uint8Array

actual fun createTestData(): Data = Data(Uint8Array("test".encodeToByteArray().toTypedArray()))
