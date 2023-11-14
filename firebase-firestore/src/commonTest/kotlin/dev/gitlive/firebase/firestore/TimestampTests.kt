package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.serialization.Serializable
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

@Serializable
data class TestData(
    val uid: String,
    val createdAt: Timestamp,
    var updatedAt: BaseTimestamp,
    val deletedAt: BaseTimestamp?
)

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
        nativeAssertEquals(
            nativeMapOf(
                "uid" to "uid123",
                "createdAt" to timestamp.nativeValue,
                "updatedAt" to timestamp.nativeValue,
                "deletedAt" to null
            ),
            encode(item, shouldEncodeElementDefault = false)
        )
    }

    @Test
    fun encodeServerTimestampObject() = runTest {
        val timestamp = Timestamp(123, 456)
        val item = TestData("uid123", timestamp, Timestamp.ServerTimestamp, Timestamp.ServerTimestamp)
        nativeAssertEquals(
            nativeMapOf(
                "uid" to "uid123",
                "createdAt" to timestamp.nativeValue,
                "updatedAt" to FieldValue.serverTimestamp.nativeValue,
                "deletedAt" to FieldValue.serverTimestamp.nativeValue
            ),
            encode(item, shouldEncodeElementDefault = false)
        )
    }

    @Test
    fun decodeTimestampObject() = runTest {
        val timestamp = Timestamp(123, 345)
        val obj = nativeMapOf(
            "uid" to "uid123",
            "createdAt" to timestamp.nativeValue,
            "updatedAt" to timestamp.nativeValue,
            "deletedAt" to timestamp.nativeValue
        )
        val decoded: TestData = decode(TestData.serializer(), obj)
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
        val obj = nativeMapOf(
            "uid" to "uid123",
            "createdAt" to Timestamp.now().nativeValue,
            "updatedAt" to Timestamp.now().nativeValue,
            "deletedAt" to null
        )
        val decoded: TestData = decode(TestData.serializer(), obj)
        assertEquals("uid123", decoded.uid)
        assertNotNull(decoded.updatedAt)
        assertNull(decoded.deletedAt)
    }

    @Test
    fun serializers() = runTest {
        //todo dont work in js due to use of reified type in firebaseSerializer - uncomment once switched to IR
//        assertEquals(BaseTimestampSerializer, (Timestamp(0, 0) as BaseTimestamp).firebaseSerializer())
//        assertEquals(BaseTimestampSerializer, (Timestamp.ServerTimestamp as BaseTimestamp).firebaseSerializer())
//        assertEquals(TimestampSerializer, Timestamp(0, 0).firebaseSerializer())
//        assertEquals(ServerTimestampSerializer, Timestamp.ServerTimestamp.firebaseSerializer())
    }

    @Test
    fun timestampMillisecondsConversion() = runTest {
        val ms = 1666170858063.0

        val timestamp = Timestamp.fromMilliseconds(ms)
        assertEquals(ms, timestamp.toMilliseconds())
    }

    @Test
    fun timestampDurationConversion() = runTest {
        val duration = 1666170858063.milliseconds
        val (seconds, nanoseconds) = duration.toComponents { seconds, nanoseconds -> seconds to nanoseconds }
        val timestamp = Timestamp.fromDuration(duration)
        assertEquals(seconds, timestamp.seconds)
        assertEquals(nanoseconds, timestamp.nanoseconds)
        assertEquals(duration, timestamp.toDuration())
    }
}
