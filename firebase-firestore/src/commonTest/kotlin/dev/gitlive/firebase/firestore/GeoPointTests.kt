package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.decode
import dev.gitlive.firebase.encode
import dev.gitlive.firebase.nativeAssertEquals
import dev.gitlive.firebase.nativeMapOf
import dev.gitlive.firebase.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class TestDataWithGeoPoint(
    val uid: String,
    val location: GeoPoint
)

@Suppress("UNCHECKED_CAST")
class GeoPointTests {

    @Test
    fun encodeGeoPointObject() = runTest {
        val geoPoint = GeoPoint(12.3, 45.6)
        val item = TestDataWithGeoPoint("123", geoPoint)
        // check GeoPoint is encoded to a platform representation
        nativeAssertEquals(
            nativeMapOf("uid" to "123", "location" to geoPoint.nativeValue),
            encode(item, shouldEncodeElementDefault = false)
        )
    }

    @Test
    fun decodeGeoPointObject() = runTest {
        val geoPoint = GeoPoint(12.3, 45.6)
        val obj = nativeMapOf(
            "uid" to "123",
            "location" to geoPoint.nativeValue
        )
        val decoded: TestDataWithGeoPoint = decode(obj)
        assertEquals("123", decoded.uid)
        // check a platform GeoPoint is properly wrapped
        assertEquals(geoPoint, decoded.location)
    }
}
