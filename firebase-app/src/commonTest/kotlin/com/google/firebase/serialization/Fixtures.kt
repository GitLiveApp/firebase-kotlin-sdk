/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalSerializationApi::class)

package com.google.firebase.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail

// Stand-ins for the product modules' mapping annotations (com.google.firebase.firestore.PropertyName and so on), declared
// as the modules' will be: recorded by kotlinx.serialization, which needs @SerialInfo and the PROPERTY target.

@SerialInfo
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class PropertyName(val value: String)

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class Exclude

@SerialInfo
@Target(AnnotationTarget.CLASS)
annotation class IgnoreExtraProperties

@SerialInfo
@Target(AnnotationTarget.CLASS)
annotation class ThrowOnExtraProperties

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class ServerTimestamp

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class DocumentId

val testAnnotations = MappingAnnotations(
    propertyName = { (it as? PropertyName)?.value },
    exclude = Exclude::class,
    ignoreExtraProperties = IgnoreExtraProperties::class,
    throwOnExtraProperties = ThrowOnExtraProperties::class,
    serverTimestamp = ServerTimestamp::class,
    documentId = DocumentId::class,
)

// Stand-ins for Firestore's value types

data class GeoPoint(val latitude: Double, val longitude: Double)

data class DocumentReference(val path: String)

object ServerTimestampSentinel

val firestore = FirebaseFormat.Firestore(
    object : FirebaseFormat.Firestore.Bindings {
        override val annotations = testAnnotations
        override val nativeTypes = listOf(GeoPoint::class, DocumentReference::class, ServerTimestampSentinel::class)
        override val documentReferenceType = DocumentReference::class
        override fun documentId(documentReference: Any) = (documentReference as DocumentReference).path.substringAfterLast('/')
        override fun documentPath(documentReference: Any) = (documentReference as DocumentReference).path
        override fun serverTimestamp(): Any = ServerTimestampSentinel
    },
)

class DatabaseException(message: String) : RuntimeException(message)

val database = FirebaseFormat.Database(testAnnotations, ::DatabaseException)

val functions = FirebaseFormat.Functions()

/**
 * Compares plain values, with numbers compared by value: on Kotlin/JS every number is a JavaScript number, so the type of
 * a number is not observable in common code (the JVM tests compare types with the Android SDK's own mappers).
 */
fun assertPlain(expected: Any?, actual: Any?, path: String = "") {
    when (expected) {
        is Number -> {
            if (actual !is Number) fail("Expected the number $expected at '$path' but was $actual")
            assertEquals(expected.toDouble(), actual.toDouble(), "at '$path'")
        }
        is Map<*, *> -> {
            if (actual !is Map<*, *>) fail("Expected a map at '$path' but was $actual")
            assertEquals(expected.keys, actual.keys, "keys at '$path'")
            for ((key, value) in expected) assertPlain(value, actual[key], "$path.$key")
        }
        is List<*> -> {
            if (actual !is List<*>) fail("Expected a list at '$path' but was $actual")
            assertEquals(expected.size, actual.size, "size at '$path'")
            expected.forEachIndexed { index, value -> assertPlain(value, actual[index], "$path[$index]") }
        }
        else -> assertEquals(expected, actual, "at '$path'")
    }
}

/** Asserts that [block] throws exactly a [T] (not a subclass) with [message], as the Android SDK's mapper does. */
inline fun <reified T : Throwable> assertFailsWithMessage(message: String, block: () -> Unit) {
    val exception = assertFailsWith<T> { block() }
    assertEquals(T::class, exception::class, "exception type")
    assertEquals(message, exception.message)
}
