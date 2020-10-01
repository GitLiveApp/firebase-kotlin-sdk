package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.decode
import dev.gitlive.firebase.encode
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.Serializable

@Serializable
data class TestData(
    val uid: String,
    @Serializable(with = FirebaseTimestampSerializer::class)
    val createdAt: Any
)

@ImplicitReflectionSerializer
@Suppress("UNCHECKED_CAST")
class TimestampTests {

    @Test
    fun encodeTimestampObject() = runTest {
        val timestamp = timestampWith(123, 456)
        val item = TestData("uid123", timestamp)
        val encoded = encode(item, shouldEncodeElementDefault = false) as Map<String, Any?>
        assertEquals("uid123", encoded["uid"])
        assertEquals(timestamp, encoded["createdAt"])
    }

    @Test
    fun encodeServerTimestampObject() = runTest {
        val timestamp = FieldValue.serverTimestamp()
        val item = TestData("uid123", timestamp)
        val encoded = encode(item, shouldEncodeElementDefault = false) as Map<String, Any?>
        assertEquals("uid123", encoded["uid"])
        assertEquals(timestamp, encoded["createdAt"])
    }

    @Test
    fun decodeTimestampObject() = runTest {
        val timestamp = timestampWith(123, 345)
        val obj = mapOf("uid" to "uid123", "createdAt" to timestamp)
        val decoded: TestData = decode(obj)
        assertEquals("uid123", decoded.uid)
        assertEquals(timestamp, decoded.createdAt)
        val createdAt: Timestamp = timestamp
        assertEquals(123, createdAt.seconds)
        assertEquals(345, createdAt.nanoseconds)
    }
}
