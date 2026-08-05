package dev.gitlive.firebase.storage

actual fun createTestData(): Data = Data("test".toByteArray())

actual fun assertTestDataEquals(data: Data) {
    kotlin.test.assertContentEquals("test".toByteArray(), data.data)
}
