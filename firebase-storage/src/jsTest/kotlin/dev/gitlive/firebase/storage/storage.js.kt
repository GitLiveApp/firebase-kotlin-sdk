package dev.gitlive.firebase.storage

import org.khronos.webgl.Uint8Array
import kotlin.test.Test
import kotlin.test.assertContentEquals

actual fun createTestData(): Data = Data(Uint8Array("test".encodeToByteArray().toTypedArray()))

actual fun assertTestDataEquals(data: Data) {
    assertContentEquals("test".encodeToByteArray().toTypedArray(), Array(data.data.length) { data.data.asDynamic()[it] as Byte })
}

class WebDataTest {

    @Test
    fun toByteArrayOnlyCopiesTheViewedBytes() {
        val view = byteArrayOf(9, 1, -128, 127, 9).toData().data.subarray(1, 4)

        assertContentEquals(byteArrayOf(1, -128, 127), Data(view).toByteArray())
    }
}
