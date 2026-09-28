/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import com.google.firebase.Timestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import dev.gitlive.firebase.IgnoreForAndroidUnitTest
import dev.gitlive.firebase.context
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.time.Instant

// The expected values and messages are those of firebase-android-sdk's firebase-firestore util/CustomClassMapper.java.

@Serializable
data class City(
    val name: String? = null,
    val state: String? = null,
    val country: String? = null,
    val isCapital: Boolean? = null,
    val population: Long? = null,
    val regions: List<String>? = null,
)

@Serializable
data class Names(val isActive: Boolean = false, val isbn: String = "", val URL: String = "", val uRLString: String = "", val _id: String = "")

@Serializable
data class Numbers(val int: Int = 0, val long: Long = 0, val double: Double = 0.0, val float: Float = 0f)

@Serializable
data class WithShort(val count: Short = 0)

@Serializable
data class WithChar(val letter: Char = 'a')

@Serializable
data class WithSet(val tags: Set<String> = emptySet())

@Serializable
data class WithArray(val tags: Array<String> = emptyArray())

@Serializable
data class WithIntKeys(val counts: Map<Int, String> = emptyMap())

@Serializable
enum class Direction {
    NORTH,

    @PropertyName("south")
    SOUTH,
}

@Serializable
data class WithEnum(val direction: Direction = Direction.NORTH)

@Serializable
data class Place(
    @Contextual val location: GeoPoint? = null,
    @Contextual val updated: Timestamp? = null,
    val instant: Instant? = null,
)

@Serializable
data class Post(
    @DocumentId val id: String = "",
    val title: String = "",
    @ServerTimestamp @Contextual val created: Timestamp? = null,
)

@Serializable
data class Linked(@DocumentId @Contextual val reference: DocumentReference? = null, val title: String = "")

@Serializable
data class BadServerTimestamp(@ServerTimestamp val created: String? = null)

@Serializable
data class Renamed(@PropertyName("full_name") val name: String = "", @Exclude val cached: String = "")

@Serializable
@ThrowOnExtraProperties
data class Strict(val name: String = "")

@Serializable
@IgnoreExtraProperties
data class Lenient(val name: String = "")

@Serializable
data class Defaults(val required: String?, val count: Int, val flag: Boolean, val withDefault: String = "default")

@Serializable
data class Nested(val cities: List<City> = emptyList())

@Serializable
data class Generic<T>(val value: T)

@Serializable
sealed class Shape {
    @Serializable
    data class Circle(val radius: Double) : Shape()
}

@Serializable
data class WithAny(val data: Map<String, @Contextual Any?> = emptyMap())

@Serializable
class Empty

class NotSerializable

class FirestoreFormatTest {

    // Initializes firebase-java-sdk's android.util.Log on the JVM, through which unknown properties are logged
    @BeforeTest
    fun initializePlatform() {
        assertNotNull(context)
    }

    @Test
    fun writesPropertiesByTheirGetterNames() {
        val city = City("Los Angeles", "CA", "USA", false, 3_900_000, arrayListOf("west_coast", "socal"))
        assertPlain(
            mapOf(
                "name" to "Los Angeles",
                "state" to "CA",
                "country" to "USA",
                "capital" to false,
                "population" to 3_900_000L,
                "regions" to listOf("west_coast", "socal"),
            ),
            firestore.encodeToPlainValue(city),
        )
        assertPlain(
            mapOf("active" to true, "isbn" to "1", "url" to "u", "urlstring" to "s", "_id" to "i"),
            firestore.encodeToPlainValue(Names(true, "1", "u", "s", "i")),
        )
    }

    @Test
    fun writesNullsAndDefaults() {
        assertPlain(
            mapOf("name" to null, "state" to null, "country" to null, "capital" to null, "population" to null, "regions" to null),
            firestore.encodeToPlainValue(City()),
        )
    }

    @Test
    fun readsPropertiesByTheirGetterNames() {
        assertEquals(
            City("Los Angeles", isCapital = true, population = 3_900_000),
            firestore.decodeFromPlainValue(hashMapOf("name" to "Los Angeles", "capital" to true, "population" to 3_900_000L)),
        )
        assertEquals(Names(isActive = true, URL = "u"), firestore.decodeFromPlainValue(hashMapOf("active" to true, "url" to "u")))
    }

    @Test
    fun writesNumbers() {
        assertPlain(mapOf("int" to 1, "long" to 2L, "double" to 3.5, "float" to 4.5f), firestore.encodeToPlainValue(Numbers(1, 2, 3.5, 4.5f)))
    }

    @Test
    fun rejectsTheValuesTheAndroidSdkRejects() {
        assertFailsWithMessage<IllegalArgumentException>(
            "Could not serialize object. Numbers of type Short are not supported, please use an int, long, float or double (found in field 'count')",
        ) { firestore.encodeToPlainValue(WithShort(1)) }
        assertFailsWithMessage<IllegalArgumentException>(
            "Could not serialize object. Characters are not supported, please use Strings (found in field 'letter')",
        ) { firestore.encodeToPlainValue(WithChar()) }
        assertFailsWithMessage<IllegalArgumentException>(
            "Could not serialize object. Serializing Collections is not supported, please use Lists instead (found in field 'tags')",
        ) { firestore.encodeToPlainValue(WithSet(setOf("a"))) }
        assertFailsWithMessage<IllegalArgumentException>(
            "Could not serialize object. Serializing Arrays is not supported, please use Lists instead (found in field 'tags')",
        ) { firestore.encodeToPlainValue(WithArray(arrayOf("a"))) }
        assertFailsWithMessage<IllegalArgumentException>(
            "Could not serialize object. Maps with non-string keys are not supported (found in field 'counts')",
        ) { firestore.encodeToPlainValue(WithIntKeys(mapOf(1 to "a"))) }
        assertFailsWithMessage<RuntimeException>("No properties to serialize found on class com.google.firebase.serialization.Empty") {
            firestore.encodeToPlainValue(Empty())
        }
    }

    @Test
    fun convertsValuesByTheirRuntimeType() {
        val point = GeoPoint(1.0, 2.0)
        assertPlain(
            mapOf("city" to mapOf("name" to "LA", "state" to null, "country" to null, "capital" to null, "population" to null, "regions" to null), "at" to point, "list" to listOf(1, "a")),
            firestore.convertToPlainValue(mapOf("city" to City("LA"), "at" to point, "list" to listOf(1, "a"))),
        )
        assertSame(point, (firestore.convertToPlainValue(mapOf("at" to point)) as Map<*, *>)["at"])
        assertFailsWithMessage<IllegalArgumentException>("Could not serialize object. Maps with non-string keys are not supported (found in field 'a')") {
            firestore.convertToPlainValue(mapOf("a" to mapOf(1 to 2)))
        }
        assertFailsWithMessage<IllegalArgumentException>(
            "Could not serialize object. Serializing Collections is not supported, please use Lists instead (found in field 'a.[0]')",
        ) { firestore.convertToPlainValue(mapOf("a" to listOf(setOf(1)))) }
        assertFailsWithMessage<IllegalArgumentException>("Could not serialize object. Characters are not supported, please use Strings") {
            firestore.convertToPlainValue('c')
        }
        val exception = kotlin.runCatching { firestore.convertToPlainValue(mapOf("a" to NotSerializable())) }.exceptionOrNull()
        assertEquals(IllegalArgumentException::class, exception!!::class)
    }

    @Test
    fun limitsTheDepth() {
        var value: Any = "leaf"
        repeat(501) { value = mapOf("a" to value) }
        assertFailsWithMessage<IllegalArgumentException>(
            "Could not serialize object. Exceeded maximum depth of 500, which likely indicates there's an object cycle (found in field '${List(501) { "a" }.joinToString(".")}')",
        ) { firestore.convertToPlainValue(value) }
    }

    @Test
    fun mapsEnumsByNameOrPropertyName() {
        assertPlain(mapOf("direction" to "NORTH"), firestore.encodeToPlainValue(WithEnum(Direction.NORTH)))
        assertPlain(mapOf("direction" to "south"), firestore.encodeToPlainValue(WithEnum(Direction.SOUTH)))
        assertEquals(WithEnum(Direction.SOUTH), firestore.decodeFromPlainValue(hashMapOf("direction" to "south")))
        assertEquals(WithEnum(Direction.SOUTH), firestore.decodeFromPlainValue(hashMapOf("direction" to "SOUTH")))
        assertFailsWithMessage<RuntimeException>(
            "Could not deserialize object. Could not find enum value of com.google.firebase.serialization.Direction for value \"EAST\" (found in field 'direction')",
        ) { firestore.decodeFromPlainValue<WithEnum>(hashMapOf("direction" to "EAST")) }
        assertFailsWithMessage<RuntimeException>(
            "Could not deserialize object. Expected a String while deserializing to enum class com.google.firebase.serialization.Direction but got a class java.lang.Boolean (found in field 'direction')",
        ) { firestore.decodeFromPlainValue<WithEnum>(hashMapOf("direction" to true)) }
    }

    @Test
    fun passesFirestoreTypesThrough() {
        val point = GeoPoint(1.0, 2.0)
        val timestamp = Timestamp(10, 20)
        val encoded = firestore.encodeToPlainValue(Place(point, timestamp, Instant.fromEpochSeconds(30, 40))) as Map<*, *>
        assertSame(point, encoded["location"])
        assertSame(timestamp, encoded["updated"])
        val instant = encoded["instant"] as Timestamp
        assertEquals(30, instant.seconds)
        assertEquals(40, instant.nanoseconds)

        val decoded = firestore.decodeFromPlainValue<Place>(hashMapOf("location" to point, "updated" to timestamp, "instant" to Timestamp(30, 40)))
        assertEquals(Place(point, timestamp, Instant.fromEpochSeconds(30, 40)), decoded)
        assertFailsWithMessage<RuntimeException>(
            "Could not deserialize object. Failed to convert value of type java.lang.String to GeoPoint (found in field 'location')",
        ) { firestore.decodeFromPlainValue<Place>(hashMapOf("location" to "here")) }
    }

    @Test
    fun writesTheServerTimestampForNull() {
        assertPlain(mapOf("title" to "t", "created" to ServerTimestampSentinel), firestore.encodeToPlainValue(Post("ignored", "t")))
        val timestamp = Timestamp(1, 2)
        assertSame(timestamp, (firestore.encodeToPlainValue(Post(title = "t", created = timestamp)) as Map<*, *>)["created"])
        assertFailsWithMessage<IllegalArgumentException>(
            "Field created is annotated with @ServerTimestamp but is class java.lang.String instead of Date, Timestamp, or Instant.",
        ) { firestore.encodeToPlainValue(BadServerTimestamp()) }
    }

    @Test
    fun readsTheDocumentId() {
        val reference = DocumentReference("posts/abc")
        assertEquals(Post("abc", "t"), firestore.decodeFromPlainValue(hashMapOf("title" to "t"), reference))
        assertEquals(Linked(reference, "t"), firestore.decodeFromPlainValue(hashMapOf("title" to "t"), reference))
        assertEquals(Post("", "t"), firestore.decodeFromPlainValue(hashMapOf("title" to "t")))
        assertFailsWithMessage<RuntimeException>(
            "'id' was found from document posts/abc, cannot apply @DocumentId on this property for class com.google.firebase.serialization.Post",
        ) { firestore.decodeFromPlainValue<Post>(hashMapOf("id" to "x", "title" to "t"), reference) }
    }

    // Logs the unknown property with android.util.Log, which Android unit tests do not provide
    @Test
    @IgnoreForAndroidUnitTest
    fun honoursPropertyNameAndExclude() {
        assertPlain(mapOf("full_name" to "n"), firestore.encodeToPlainValue(Renamed("n", "c")))
        assertEquals(Renamed("n"), firestore.decodeFromPlainValue(hashMapOf("full_name" to "n", "cached" to "c")))
    }

    // Logs the unknown property with android.util.Log, which Android unit tests do not provide
    @Test
    @IgnoreForAndroidUnitTest
    fun handlesExtraProperties() {
        assertEquals(City("a"), firestore.decodeFromPlainValue(hashMapOf("name" to "a", "unknown" to 1)))
        assertEquals(Lenient("a"), firestore.decodeFromPlainValue(hashMapOf("name" to "a", "unknown" to 1)))
        assertFailsWithMessage<RuntimeException>("No setter/field for unknown found on class com.google.firebase.serialization.Strict") {
            firestore.decodeFromPlainValue<Strict>(hashMapOf("name" to "a", "unknown" to 1))
        }
        assertFailsWithMessage<RuntimeException>(
            "No setter/field for Name found on class com.google.firebase.serialization.Strict (fields/setters are case sensitive!)",
        ) { firestore.decodeFromPlainValue<Strict>(hashMapOf("Name" to "a")) }
    }

    @Test
    fun keepsDefaultsForMissingProperties() {
        assertEquals(Defaults(null, 0, false), firestore.decodeFromPlainValue(hashMapOf<String, Any?>()))
        assertEquals(City(), firestore.decodeFromPlainValue(hashMapOf("population" to null, "name" to null)))
        assertEquals(Numbers(), firestore.decodeFromPlainValue(hashMapOf("int" to null)))
    }

    @Test
    fun convertsNumbersAsTheAndroidSdk() {
        assertEquals(Numbers(int = 1, long = 2, double = 3.0), firestore.decodeFromPlainValue(hashMapOf("int" to 1L, "long" to 2, "double" to 3L)))
        assertEquals(Numbers(int = 1, long = 2), firestore.decodeFromPlainValue(hashMapOf("int" to 1.9, "long" to 2.9)))
        assertFailsWithMessage<RuntimeException>(
            "Could not deserialize object. Failed to convert a value of type java.lang.String to int (found in field 'int')",
        ) { firestore.decodeFromPlainValue<Numbers>(hashMapOf("int" to "1")) }
        assertFailsWithMessage<RuntimeException>(
            "Could not deserialize object. Failed to convert value of type java.lang.Long to boolean (found in field 'capital')",
        ) { firestore.decodeFromPlainValue<City>(hashMapOf("capital" to 1L)) }
        assertFailsWithMessage<RuntimeException>(
            "Could not deserialize object. Failed to convert value of type java.lang.Long to String (found in field 'name')",
        ) { firestore.decodeFromPlainValue<City>(hashMapOf("name" to 1L)) }
        assertFailsWithMessage<RuntimeException>("Could not deserialize object. Deserializing values to short is not supported (found in field 'count')") {
            firestore.decodeFromPlainValue<WithShort>(hashMapOf("count" to 1L))
        }
    }

    @Test
    fun convertsLargeNumbersAsTheAndroidSdk() {
        assertFailsWithMessage<RuntimeException>(
            "Could not deserialize object. Loss of precision while converting number to double: 9007199254740993. Did you mean to use a 64-bit long instead? (found in field 'double')",
        ) { firestore.decodeFromPlainValue<Numbers>(hashMapOf("double" to 9_007_199_254_740_993L)) }
        val exception = kotlin.runCatching { firestore.decodeFromPlainValue<Numbers>(hashMapOf("int" to 3_000_000_000L)) }.exceptionOrNull()
        assertEquals(RuntimeException::class, exception!!::class)
    }

    @Test
    fun rejectsStructuresTheAndroidSdkRejects() {
        assertFailsWithMessage<RuntimeException>(
            "Could not deserialize object. Can't convert object of type java.lang.String to type com.google.firebase.serialization.City (found in field 'cities.[0]')",
        ) { firestore.decodeFromPlainValue<Nested>(hashMapOf("cities" to arrayListOf("LA"))) }
        assertFailsWithMessage<RuntimeException>(
            "Could not deserialize object. Expected a List, but got a class java.lang.String (found in field 'cities')",
        ) { firestore.decodeFromPlainValue<Nested>(hashMapOf("cities" to "LA")) }
        assertFailsWithMessage<RuntimeException>("Could not deserialize object. Collections are not supported, please use Lists instead (found in field 'tags')") {
            firestore.decodeFromPlainValue<WithSet>(hashMapOf("tags" to arrayListOf("a")))
        }
        assertFailsWithMessage<RuntimeException>("Could not deserialize object. Converting to Arrays is not supported, please use Lists instead (found in field 'tags')") {
            firestore.decodeFromPlainValue<WithArray>(hashMapOf("tags" to arrayListOf("a")))
        }
        assertFailsWithMessage<RuntimeException>(
            "Could not deserialize object. Only Maps with string keys are supported, but found Map with key type class java.lang.Integer (found in field 'counts')",
        ) { firestore.decodeFromPlainValue<WithIntKeys>(hashMapOf("counts" to hashMapOf("1" to "a"))) }
        assertFailsWithMessage<RuntimeException>(
            "Could not deserialize object. Expected a Map while deserializing, but got a class java.util.ArrayList (found in field 'data')",
        ) { firestore.decodeFromPlainValue<WithAny>(hashMapOf("data" to arrayListOf(1))) }
        assertFailsWithMessage<RuntimeException>(
            "Could not deserialize object. Failed to convert a value of type java.lang.String to long (found in field 'cities.[1].population')",
        ) { firestore.decodeFromPlainValue<Nested>(hashMapOf("cities" to arrayListOf(hashMapOf("name" to "a"), hashMapOf("population" to "many")))) }
    }

    @Test
    fun mapsAnyToThePlainValue() {
        val data = hashMapOf("a" to 1L, "b" to arrayListOf("x"), "c" to hashMapOf("d" to null))
        assertEquals(data, firestore.decodeFromPlainValue<Map<String, Any?>>(data))
        assertEquals(WithAny(data), firestore.decodeFromPlainValue(hashMapOf("data" to data)))
        assertPlain(mapOf("data" to mapOf("city" to mapOf("name" to "LA", "state" to null, "country" to null, "capital" to null, "population" to null, "regions" to null))), firestore.encodeToPlainValue(WithAny(mapOf("city" to City("LA")))))
        assertNull(firestore.convertToCustomClass(null, City::class))
        assertEquals(City("LA"), firestore.convertToCustomClass(hashMapOf("name" to "LA"), City::class))
    }

    @Test
    fun supportsWhatKotlinxSerializationAddsToTheAndroidSdk() {
        assertEquals(Generic(City("LA")), firestore.decodeFromPlainValue<Generic<City>>(hashMapOf("value" to hashMapOf("name" to "LA"))))
        assertPlain(mapOf("value" to 1), firestore.encodeToPlainValue(Generic(1)))
        val circle: Shape = Shape.Circle(1.5)
        val encoded = firestore.encodeToPlainValue(circle)
        assertPlain(mapOf("type" to "com.google.firebase.serialization.Shape.Circle", "radius" to 1.5), encoded)
        assertEquals(circle, firestore.decodeFromPlainValue<Shape>(encoded))
        assertEquals(listOf(1L, 2L), firestore.decodeFromPlainValue(ListSerializerOfLong, arrayListOf(1L, 2)))
    }
}

private val ListSerializerOfLong = kotlinx.serialization.builtins.ListSerializer(Long.serializer())
