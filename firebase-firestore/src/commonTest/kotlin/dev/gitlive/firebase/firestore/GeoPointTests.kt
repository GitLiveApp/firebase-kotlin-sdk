package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.decode
import dev.gitlive.firebase.encode
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class TestDataWithGeoPoint(
    val uid: String,
    @Serializable(with = FirebaseGeoPointSerializer::class)
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
        assertEquals(geoPoint, encoded["location"])
    }

    @Test
    fun decodeGeoPointObject() = runTest {
        val geoPoint = GeoPoint(12.3, 45.6)
        val obj = mapAsEncoded(mapOf("uid" to "123", "location" to geoPoint))
        val decoded: TestDataWithGeoPoint = decode(obj)
        assertEquals("123", decoded.uid)
        assertEquals(geoPoint, decoded.location)
    }
}
