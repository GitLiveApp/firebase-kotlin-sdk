package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.decode
import dev.gitlive.firebase.encode
import dev.gitlive.firebase.firebaseSerializer
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
        val encoded = encodedAsMap(encode(item, shouldEncodeElementDefault = false))
        assertEquals("123", encoded["uid"])
        // check GeoPoint is encoded to a platform representation
        assertEquals(geoPoint.nativeValue, encoded["location"])
    }

    @Test
    fun decodeGeoPointObject() = runTest {
        val geoPoint = GeoPoint(12.3, 45.6)
        val obj = mapOf(
            "uid" to "123",
            "location" to geoPoint.nativeValue
        ).asEncoded()
        val decoded: TestDataWithGeoPoint = decode(obj)
        assertEquals("123", decoded.uid)
        // check a platform GeoPoint is properly wrapped
        assertEquals(geoPoint, decoded.location)
    }

    @Test
    @IgnoreJs
    fun serializers() = runTest {
        assertEquals(GeoPointSerializer, GeoPoint(0.0,0.0).firebaseSerializer())
    }
}
