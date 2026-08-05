package dev.gitlive.firebase.storage

import kotlin.test.assertContentEquals

actual fun createTestData(): Data = Data("test".toByteArray())

actual fun assertTestDataEquals(data: Data) {
    assertContentEquals("test".toByteArray(), data.data)
}
