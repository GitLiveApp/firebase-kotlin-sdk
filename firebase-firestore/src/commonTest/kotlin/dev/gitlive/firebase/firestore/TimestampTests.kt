package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.decode
import dev.gitlive.firebase.encode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.Serializable

@Serializable
data class TestData(
    val uid: String,
    @Serializable(with = FirebaseTimestampSerializer::class)
    val createdAt: Any,
    @Serializable(with = FirebaseNullableTimestampSerializer::class)
    var updatedAt: Any?,
    @Serializable(with = TimestampNullableSerializer::class)
    var deletedAt: FirebaseTimestamp?
)

@Suppress("UNCHECKED_CAST")
class TimestampTests {

    @Test
    fun encodeTimestampObject() = runTest {
        val timestamp = timestampWith(123, 456)
        val item = TestData("uid123", timestamp, null, FirebaseTimestamp.Value(timestamp))
        val encoded = encodedAsMap(encode(item, shouldEncodeElementDefault = false))
        assertEquals("uid123", encoded["uid"])
        assertEquals(timestamp, encoded["createdAt"])
        assertEquals(timestamp, encoded["deletedAt"])
    }

    @Test
    fun encodeServerTimestampObject() = runTest {
        val timestamp = FieldValue.serverTimestamp()
        val item = TestData("uid123", timestamp, null, FirebaseTimestamp.ServerValue)
        val encoded = encodedAsMap(encode(item, shouldEncodeElementDefault = false))
        assertEquals("uid123", encoded["uid"])
        assertEquals(timestamp, encoded["createdAt"])
        customAssertEquals(FieldValue.serverTimestamp(), encoded["deletedAt"])
    }

    @Test
    fun decodeTimestampObject() = runTest {
        val timestamp = timestampWith(123, 345)
        val obj = mapAsEncoded(mapOf("uid" to "uid123", "createdAt" to timestamp, "deletedAt" to timestamp))
        val decoded: TestData = decode(obj)
        assertEquals("uid123", decoded.uid)
        assertEquals(timestamp, decoded.createdAt)
        val createdAt: Timestamp = timestamp
        assertEquals(123, createdAt.seconds)
        assertEquals(345, createdAt.nanoseconds)
        val deletedAt: Timestamp? = decoded.deletedAt?.timestamp
        assertEquals(123, deletedAt?.seconds)
        assertEquals(345, deletedAt?.nanoseconds)
    }

    @Test
    fun decodeEmptyTimestampObject() = runTest {
        val obj = mapAsEncoded(mapOf("uid" to "uid123", "createdAt" to timestampNow(), "updatedAt" to null))
        val decoded: TestData = decode(obj)
        assertEquals("uid123", decoded.uid)
        assertEquals(null, decoded.updatedAt)
    }

    @Test
    fun decodeDeletedTimestampObject() = runTest {
        val timestamp = timestampWith(123, 345)
        val obj = mapAsEncoded(mapOf(
            "uid" to "uid123",
            "createdAt" to timestampNow(),
            "updatedAt" to FieldValue.delete,
            "deletedAt" to FirebaseTimestamp.ServerDelete
        ))
        val decoded: TestData = decode(obj)

        assertEquals("uid123", decoded.uid)
        assertEquals(null, decoded.updatedAt)
        assertEquals(null, decoded.deletedAt)
    }

    @Test
    fun encodeDeletedTimestampObject() = runTest {
        val timestamp = FieldValue.delete
        val item = TestData("uid123", timestamp, null, FirebaseTimestamp.ServerDelete)
        val encoded = encodedAsMap(encode(item, shouldEncodeElementDefault = false))
        assertEquals("uid123", encoded["uid"])
        assertEquals(timestamp, encoded["createdAt"])
        customAssertEquals(FieldValue.delete, encoded["deletedAt"])
    }
}
