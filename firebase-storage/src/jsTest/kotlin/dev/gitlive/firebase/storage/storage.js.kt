package dev.gitlive.firebase.storage

import org.khronos.webgl.Uint8Array
import kotlin.test.assertContentEquals

actual fun createTestData(): Data = Data(Uint8Array("test".encodeToByteArray().toTypedArray()))

actual fun assertTestDataEquals(data: Data) {
    assertContentEquals("test".encodeToByteArray().toTypedArray(), Array(data.data.length) { data.data.asDynamic()[it] as Byte })
}
