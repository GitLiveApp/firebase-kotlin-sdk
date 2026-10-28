/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import dev.gitlive.firebase.context
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.lang.reflect.InvocationTargetException
import java.util.Date
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail
import com.google.firebase.database.DatabaseException as AndroidDatabaseException
import com.google.firebase.database.core.utilities.encoding.CustomClassMapper as DatabaseMapper
import com.google.firebase.firestore.util.CustomClassMapper as FirestoreMapper

@Serializable
data class NativeValues(
    @Contextual val location: GeoPoint? = null,
    @Contextual val updated: Timestamp? = null,
    @Contextual val date: Date? = null,
    @Contextual val bytes: Blob? = null,
)

@Serializable
data class Readable(
    val name: String? = null,
    val population: Long? = null,
    val int: Int = 0,
    val double: Double = 0.0,
    val float: Float = 0f,
    val direction: Direction = Direction.NORTH,
    val regions: List<String>? = null,
    val counts: Map<String, Long>? = null,
    val nested: Readable? = null,
)

/**
 * Compares the format with the class mappers of the Firebase Android SDK itself (bundled in firebase-java-sdk): for the
 * same Kotlin value, the same plain values or the same exception, of the same type and with the same message.
 *
 * The Android SDK's annotations are Java annotations, which kotlinx.serialization does not record, so these tests do not
 * use annotations; the common tests cover them.
 */
class AndroidMapperParityTest {

    private val firestoreFormat = FirebaseFormat.Firestore(
        object : FirebaseFormat.Firestore.Bindings {
            override val annotations = MappingAnnotations()
            override val nativeTypes = listOf(GeoPoint::class, Blob::class, DocumentReference::class, FieldValue::class, Date::class)
            override val documentReferenceType = DocumentReference::class
            override fun documentId(documentReference: Any) = (documentReference as DocumentReference).id
            override fun documentPath(documentReference: Any) = (documentReference as DocumentReference).path
            override fun serverTimestamp(): Any = FieldValue.serverTimestamp()
        },
    )

    private val databaseFormat = FirebaseFormat.Database(MappingAnnotations(), ::AndroidDatabaseException)

    private var values = 0
    private var exceptions = 0

    @BeforeTest
    fun initializePlatform() {
        assertNotNull(context)
    }

    // Each test compares values and exceptions both, so it cannot pass by failing the same way throughout
    @AfterTest
    fun checkBothOutcomesWereCompared() {
        assertTrue(values > 0 && exceptions > 0, "values: $values, exceptions: $exceptions")
    }

    private val encoded = listOf(
        City("Los Angeles", "CA", "USA", false, 3_900_000, arrayListOf("west_coast", "socal")),
        City(),
        Names(true, "1", "u", "s", "i"),
        Numbers(1, 2, 3.0, 4.5f),
        Numbers(1, 2, 3.5, 4f),
        WithEnum(Direction.NORTH),
        WithEnum(Direction.SOUTH),
        Nested(listOf(City("a"), City("b", population = 2))),
        NativeValues(GeoPoint(1.0, 2.0), Timestamp(3, 4), Date(5), Blob.fromBytes(byteArrayOf(6))),
        Readable("n", 1, 2, 3.5, 4.5f, Direction.SOUTH, listOf("r"), mapOf("c" to 5L), Readable("m")),
        Score("a", 1.0, true),
        WithShort(1),
        WithChar(),
        WithSet(setOf("a")),
        WithArray(arrayOf("a")),
        WithIntKeys(mapOf(1 to "a")),
        Empty(),
    )

    private val runtimeValues = listOf(
        mapOf("a" to listOf(1, 2L, 3.5, 4.5f, "s", true, null), "b" to mapOf("c" to mapOf("d" to 1))),
        mapOf("city" to City("LA"), "place" to GeoPoint(1.0, 2.0), "server" to FieldValue.serverTimestamp()),
        mapOf("a" to mapOf(1 to 2)),
        mapOf("a" to listOf(setOf(1))),
        mapOf("a" to arrayOf(1)),
        mapOf("a" to 1.toShort()),
        mapOf("a" to 'c'),
        mapOf("direction" to Direction.SOUTH),
        1.0,
        2.5f,
    )

    private val decoded: List<Pair<Map<String, Any?>, Class<*>>> = listOf(
        hashMapOf<String, Any?>("name" to "n", "population" to 1L, "int" to 2L, "double" to 3L, "float" to 4.5, "direction" to "SOUTH") to Readable::class.java,
        hashMapOf<String, Any?>("regions" to arrayListOf("a"), "counts" to hashMapOf("c" to 1L), "nested" to hashMapOf("name" to "m")) to Readable::class.java,
        hashMapOf<String, Any?>("int" to 1.9, "double" to 2.5) to Readable::class.java,
        hashMapOf<String, Any?>("name" to null, "nested" to null) to Readable::class.java,
        hashMapOf<String, Any?>("int" to "1") to Readable::class.java,
        hashMapOf<String, Any?>("int" to 3_000_000_000L) to Readable::class.java,
        hashMapOf<String, Any?>("population" to 1.0e19) to Readable::class.java,
        hashMapOf<String, Any?>("double" to 9_007_199_254_740_993L) to Readable::class.java,
        hashMapOf<String, Any?>("name" to 1L) to Readable::class.java,
        hashMapOf<String, Any?>("direction" to "EAST") to Readable::class.java,
        hashMapOf<String, Any?>("direction" to true) to Readable::class.java,
        hashMapOf<String, Any?>("regions" to "a") to Readable::class.java,
        hashMapOf<String, Any?>("counts" to arrayListOf(1L)) to Readable::class.java,
        hashMapOf<String, Any?>("nested" to "n") to Readable::class.java,
        hashMapOf<String, Any?>("regions" to arrayListOf(1L)) to Readable::class.java,
        hashMapOf<String, Any?>("unknown" to 1L, "Name" to "n") to Readable::class.java,
        hashMapOf<String, Any?>("count" to 1L) to WithShort::class.java,
        hashMapOf<String, Any?>("tags" to arrayListOf("a")) to WithSet::class.java,
        hashMapOf<String, Any?>("tags" to arrayListOf("a")) to WithArray::class.java,
        hashMapOf<String, Any?>("counts" to hashMapOf("1" to "a")) to WithIntKeys::class.java,
        hashMapOf<String, Any?>("cities" to arrayListOf("LA")) to Nested::class.java,
        hashMapOf<String, Any?>("location" to GeoPoint(1.0, 2.0), "updated" to Timestamp(3, 4), "bytes" to Blob.fromBytes(byteArrayOf(1))) to NativeValues::class.java,
        hashMapOf<String, Any?>("location" to "here") to NativeValues::class.java,
        hashMapOf<String, Any?>() to Empty::class.java,
    )

    @Test
    fun firestoreWritesAsTheAndroidSdk() {
        for (value in encoded) {
            assertSameOutcome("$value") { format -> if (format) firestoreFormat.convertToPlainValue(value) else FirestoreMapper.convertToPlainJavaTypes(value) }
        }
        for (value in runtimeValues) {
            assertSameOutcome("$value") { format -> if (format) firestoreFormat.convertToPlainValue(value) else FirestoreMapper.convertToPlainJavaTypes(value) }
        }
    }

    @Test
    fun firestoreReadsAsTheAndroidSdk() {
        for ((value, type) in decoded) {
            assertSameOutcome("$value as ${type.simpleName}") { format ->
                if (format) firestoreFormat.convertToCustomClass(value, type.kotlin) else FirestoreMapper.convertToCustomClass(value, type, null)
            }
        }
    }

    @Test
    fun databaseWritesAsTheAndroidSdk() {
        for (value in encoded.filter { it !is NativeValues } + runtimeValues.filter { !it.toString().contains("GeoPoint") }) {
            assertSameOutcome("$value") { format -> if (format) databaseFormat.convertToPlainValue(value) else DatabaseMapper.convertToPlainJavaTypes(value) }
        }
    }

    @Test
    fun databaseReadsAsTheAndroidSdk() {
        for ((value, type) in decoded.filter { it.second != NativeValues::class.java }) {
            assertSameOutcome("$value as ${type.simpleName}") { format ->
                if (format) databaseFormat.convertToCustomClass(value, type.kotlin) else DatabaseMapper.convertToCustomClass(value, type)
            }
        }
    }

    @Test
    fun functionsAcceptsTheCallDataOfTheAndroidSdk() {
        val serializer = Class.forName("com.google.firebase.functions.Serializer").getDeclaredConstructor().apply { isAccessible = true }.newInstance()
        val encode = serializer.javaClass.getMethod("encode", Any::class.java).apply { isAccessible = true }
        val decode = serializer.javaClass.getMethod("decode", Any::class.java).apply { isAccessible = true }
        fun json(value: Any?) = try {
            decode.invoke(serializer, encode.invoke(serializer, value))
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
        val values = runtimeValues + listOf(
            mapOf("long" to Long.MAX_VALUE, "short" to 1.toShort(), "byte" to 2.toByte()),
            listOf(Direction.NORTH),
            Score(),
            setOf(1),
            mapOf(1 to 2),
            'c',
        )
        for (value in values.filter { !it.toString().contains("GeoPoint") }) {
            // What the Android SDK sends for the plain values the format produces is what it sends for the value itself
            assertSameOutcome("$value") { format -> if (format) json(FirebaseFormat.Functions().convertToPlainValue(value)) else json(value) }
        }
    }

    /** Runs [block] with the Android SDK's mapper (`false`) and the format (`true`) and compares the outcomes. */
    private fun assertSameOutcome(description: String, block: (format: Boolean) -> Any?) {
        val expected = runCatching { block(false) }
        val actual = runCatching { block(true) }
        val exception = expected.exceptionOrNull()
        if (exception is ReflectiveOperationException) throw exception
        if (exception == null) values++ else exceptions++
        if (exception == null) {
            assertEquals(expected.getOrNull(), actual.getOrElse { throw AssertionError("$description: expected ${expected.getOrNull()}", it) }, description)
        } else {
            val actualException = actual.exceptionOrNull() ?: fail("$description: expected $exception but was ${actual.getOrNull()}")
            assertEquals(exception::class, actualException::class, "$description: $actualException")
            assertEquals(exception.message, actualException.message, description)
        }
    }
}
