package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.decode
import dev.gitlive.firebase.encode
import dev.gitlive.firebase.firebaseSerializer
import kotlinx.serialization.Serializable
import kotlin.test.*

@Serializable
data class TestData(
    val uid: String,
    val createdAt: Timestamp,
    var updatedAt: BaseTimestamp,
    val deletedAt: BaseTimestamp?
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
        assertEquals(Timestamp.ServerTimestamp, Timestamp.ServerTimestamp)
    }

    @Test
    fun encodeTimestampObject() = runTest {
        val timestamp = Timestamp(123, 456)
        val item = TestData("uid123", timestamp, timestamp, null)
        val encoded = encodedAsMap(encode(item, shouldEncodeElementDefault = false))
        assertEquals("uid123", encoded["uid"])
        // NOTE: wrapping is required because JS does not override equals
        assertEquals(timestamp, Timestamp(encoded["createdAt"] as NativeTimestamp))
        assertEquals(timestamp, Timestamp(encoded["updatedAt"] as NativeTimestamp))
        assertNull(encoded["deletedAt"])
    }

    @Test
    fun encodeServerTimestampObject() = runTest {
        val timestamp = Timestamp(123, 456)
        val item = TestData("uid123", timestamp, Timestamp.ServerTimestamp, Timestamp.ServerTimestamp)
        val encoded = encodedAsMap(encode(item, shouldEncodeElementDefault = false))
        assertEquals("uid123", encoded["uid"])
        assertEquals(timestamp, Timestamp(encoded["createdAt"] as NativeTimestamp))
        assertEquals(FieldValue.serverTimestamp, FieldValue(encoded["updatedAt"]!!))
        assertEquals(FieldValue.serverTimestamp, FieldValue(encoded["deletedAt"]!!))
    }

    @Test
    fun decodeTimestampObject() = runTest {
        val timestamp = Timestamp(123, 345)
        val obj = mapOf(
            "uid" to "uid123",
            "createdAt" to timestamp.nativeValue,
            "updatedAt" to timestamp.nativeValue,
            "deletedAt" to timestamp.nativeValue
        ).asEncoded()
        val decoded: TestData = decode(obj)
        assertEquals("uid123", decoded.uid)
        with(decoded.createdAt) {
            assertEquals(timestamp, this)
            assertEquals(123, seconds)
            assertEquals(345, nanoseconds)
        }
        with(decoded.updatedAt as Timestamp) {
            assertEquals(timestamp, this)
            assertEquals(123, seconds)
            assertEquals(345, nanoseconds)
        }
        with(decoded.deletedAt as Timestamp) {
            assertEquals(timestamp, this)
            assertEquals(123, seconds)
            assertEquals(345, nanoseconds)
        }
    }

    @Test
    fun decodeEmptyTimestampObject() = runTest {
        val obj = mapOf(
            "uid" to "uid123",
            "createdAt" to Timestamp.now().nativeValue,
            "updatedAt" to Timestamp.now().nativeValue,
            "deletedAt" to null
        ).asEncoded()
        val decoded: TestData = decode(obj)
        assertEquals("uid123", decoded.uid)
        assertNotNull(decoded.updatedAt)
        assertNull(decoded.deletedAt)
    }

    @Test
    @IgnoreJs
    fun serializers() = runTest {
        assertEquals(BaseTimestampSerializer, (Timestamp(0, 0) as BaseTimestamp).firebaseSerializer())
        assertEquals(BaseTimestampSerializer, (Timestamp.ServerTimestamp as BaseTimestamp).firebaseSerializer())
        assertEquals(TimestampSerializer, Timestamp(0, 0).firebaseSerializer())
        assertEquals(ServerTimestampSerializer, Timestamp.ServerTimestamp.firebaseSerializer())
    }

    @Test
    fun timestampMillisecondsConversion() = runTest {
        val ms = 1666170858063
        val seconds = 1666170858
        val nanoseconds = 63000000 // 1 millisecond = 1000 microseconds = 1000000 nanoseconds
        val timestamp = Timestamp.fromMilliseconds(ms)

        assertEquals(seconds.toLong(), timestamp.seconds)
        assertEquals(nanoseconds, timestamp.nanoseconds)

        assertEquals(ms, timestamp.toMilliseconds())
    }
}
