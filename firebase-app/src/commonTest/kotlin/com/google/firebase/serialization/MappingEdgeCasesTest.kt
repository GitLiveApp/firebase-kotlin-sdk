/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import com.google.firebase.Timestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@Serializable
sealed class Marker {
    @Serializable
    object Start : Marker()

    @Serializable
    data class At(val position: Int) : Marker()
}

@Serializable
object Singleton

@Serializable
sealed class Tagged {
    @Serializable
    data class WithType(val type: String) : Tagged()
}

@Serializable
data class CaseDistinct(@SerialName("X-ID") val a: Int = 0, @SerialName("x-id") val b: Int = 0)

@Serializable
data class WithDouble(val n: Double = 0.0)

@Serializable
data class WithBlob(val data: @Contextual Blob? = null, val any: @Contextual Any? = null)

class MappingEdgeCasesTest {

    @Test
    fun mapsSerializableObjects() {
        assertPlain(emptyMap<String, Any?>(), firestore.encodeToPlainValue(Singleton))
        assertSame(Singleton, firestore.decodeFromPlainValue<Singleton>(hashMapOf<String, Any?>()))
        val stored = mapOf("type" to "com.google.firebase.serialization.Marker.Start")
        assertPlain(stored, database.encodeToPlainValue<Marker>(Marker.Start))
        assertSame(Marker.Start, database.decodeFromPlainValue<Marker>(HashMap(stored)))
    }

    @Test
    fun rejectsAPropertyNamedLikeTheSubclassNameProperty() {
        assertFailsWithMessage<IllegalArgumentException>(
            "Could not serialize object. Property 'type' of com.google.firebase.serialization.Tagged.WithType conflicts with the property that stores the subclass name of a polymorphic value",
        ) { firestore.encodeToPlainValue<Tagged>(Tagged.WithType("a")) }
    }

    @Test
    fun convertsToNativeAndAnyTargets() {
        val timestamp = Timestamp(1, 2)
        assertSame(timestamp, firestore.convertToCustomClass(timestamp, Timestamp::class))
        val point = GeoPoint(1.0, 2.0)
        assertSame(point, firestore.convertToCustomClass(point, GeoPoint::class))
        val map = hashMapOf<String, Any?>("a" to 1)
        assertSame(map, firestore.convertToCustomClass(map, Any::class))
        assertFailsWithMessage<RuntimeException>("Could not deserialize object. Failed to convert value of type java.lang.String to Timestamp") {
            firestore.convertToCustomClass("a", Timestamp::class)
        }
        assertFailsWithMessage<RuntimeException>("Class NotSerializable is not @Serializable") { firestore.convertToCustomClass(map, NotSerializable::class) }
    }

    @Test
    fun functionsKeepsCaseDistinctSerialNames() {
        assertPlain(mapOf("X-ID" to 1, "x-id" to 2), functions.encodeToPlainValue(CaseDistinct(1, 2)))
        assertEquals(CaseDistinct(1, 2), functions.decodeFromPlainValue(hashMapOf("X-ID" to 1, "x-id" to 2)))
        assertFailsWithMessage<RuntimeException>("Found two getters or fields with conflicting case sensitivity for property: x-id") {
            firestore.encodeToPlainValue(CaseDistinct())
        }
    }

    @Test
    fun convertsTheProductsValueTypesThroughTheBindings() {
        assertEquals(SdkBlob("x"), (toKotlin(firestore.encodeToPlainValue(WithBlob(Blob("x")))) as Map<*, *>)["data"])
        assertEquals(SdkBlob("y"), (toKotlin(firestore.convertToPlainValue(mapOf("b" to Blob("y")))) as Map<*, *>)["b"])
        val stored = hashMapOf("data" to SdkBlob("x"), "any" to hashMapOf("b" to SdkBlob("z")))
        assertEquals(WithBlob(Blob("x"), mapOf("b" to Blob("z"))), firestore.decodeFromPlainValue<WithBlob>(stored))
        assertEquals(Blob("x"), firestore.convertToCustomClass(SdkBlob("x"), Blob::class))
        assertEquals(mapOf("b" to Blob("z")), firestore.convertToCustomClass(hashMapOf("b" to SdkBlob("z")), Any::class))
    }

    @Test
    fun findsSerializersForNativeTypesAndGenericArguments() {
        val timestamp = Timestamp(1, 2)
        assertSame(timestamp, firestore.decodeFromPlainValue<Timestamp>(timestamp))
        assertSame(timestamp, (toKotlin(firestore.encodeToPlainValue(listOf(timestamp))) as List<*>)[0])
        assertPlain(mapOf("value" to mapOf("a" to 1)), firestore.encodeToPlainValue(Generic<Any?>(mapOf("a" to 1))))
        assertEquals(Generic<Any?>(mapOf("a" to 1)), firestore.decodeFromPlainValue<Generic<Any?>>(hashMapOf("value" to hashMapOf("a" to 1))))
    }

    @Test
    fun keepsNaN() {
        val plain = toKotlin(firestore.convertToPlainValue(mapOf("n" to Double.NaN))) as Map<*, *>
        assertTrue((plain["n"] as Double).isNaN())
        assertTrue(firestore.decodeFromPlainValue<WithDouble>(hashMapOf("n" to Double.NaN)).n.isNaN())
    }
}
