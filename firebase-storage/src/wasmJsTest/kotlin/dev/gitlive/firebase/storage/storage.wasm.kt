package dev.gitlive.firebase.storage

import org.khronos.webgl.Uint8Array
import kotlin.test.assertContentEquals

actual fun createTestData(): Data = Data(stringToUint8Array("test"))

actual fun assertTestDataEquals(data: Data) {
    val actual = ByteArray(data.data.length) { uint8At(data.data, it).toByte() }
    assertContentEquals("test".encodeToByteArray(), actual)
}

private fun uint8At(array: Uint8Array, index: Int): Int = js("array[index]")

private fun stringToUint8Array(value: String): Uint8Array = js("new TextEncoder().encode(value)")
