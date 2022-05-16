package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.decode
import dev.gitlive.firebase.encode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.Serializable
import kotlin.test.assertNotEquals

@Serializable
data class TestData(
    val uid: String,
    val createdAt: Timestamp,
    var updatedAt: Timestamp?
)

@Suppress("UNCHECKED_CAST")
class TimestampTests {
    @Test
    fun testEquality() = runTest {
        val timestamp = Timestamp(123, 456)
        assertEquals(timestamp, Timestamp(123, 456))
        assertNotEquals(timestamp, Timestamp(123, 457))
        assertNotEquals(timestamp, Timestamp(124, 456))
        assertNotEquals(timestamp, Timestamp.now())
        assertEquals(Timestamp.serverTimestamp(), Timestamp.serverTimestamp())
    }

    @Test
    fun encodeTimestampObject() = runTest {
        val timestamp = Timestamp(123, 456)
        val item = TestData("uid123", timestamp, timestamp)
        val encoded = encodedAsMap(encode(item, shouldEncodeElementDefault = false))
        assertEquals("uid123", encoded["uid"])
        // NOTE: wrapping is required because JS does not override equals
        assertEquals(timestamp, Timestamp(encoded["createdAt"] as PlatformTimestamp))
        assertEquals(timestamp, Timestamp(encoded["updatedAt"] as PlatformTimestamp))
    }

    @Test
    fun encodeServerTimestampObject() = runTest {
        val item = TestData("uid123", Timestamp.serverTimestamp(), Timestamp.serverTimestamp())
        val encoded = encodedAsMap(encode(item, shouldEncodeElementDefault = false))
        assertEquals("uid123", encoded["uid"])
        assertEquals(FieldValue.serverTimestamp(), FieldValue(encoded["createdAt"]!!))
        assertEquals(FieldValue.serverTimestamp(), FieldValue(encoded["updatedAt"]!!))
    }

    @Test
    fun decodeTimestampObject() = runTest {
        val timestamp = Timestamp(123, 345)
        val obj = mapAsEncoded(mapOf("uid" to "uid123", "createdAt" to timestamp.platformValue, "updatedAt" to timestamp.platformValue))
        val decoded: TestData = decode(obj)
        assertEquals("uid123", decoded.uid)
        assertEquals(timestamp, decoded.createdAt)
        assertEquals(123, decoded.createdAt.seconds)
        assertEquals(345, decoded.createdAt.nanoseconds)
        assertEquals(123, decoded.updatedAt?.seconds)
        assertEquals(345, decoded.updatedAt?.nanoseconds)
    }

    @Test
    fun decodeEmptyTimestampObject() = runTest {
        val obj = mapAsEncoded(mapOf("uid" to "uid123", "createdAt" to Timestamp.now().platformValue, "updatedAt" to null))
        val decoded: TestData = decode(obj)
        assertEquals("uid123", decoded.uid)
        assertEquals(null, decoded.updatedAt)
    }
}
