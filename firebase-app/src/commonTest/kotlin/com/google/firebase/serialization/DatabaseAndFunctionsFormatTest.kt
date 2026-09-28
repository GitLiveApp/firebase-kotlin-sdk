/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlinx.serialization.Serializable
import dev.gitlive.firebase.context
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

// The expected values and messages are those of firebase-android-sdk's firebase-database
// core/utilities/encoding/CustomClassMapper.java and firebase-functions Serializer.kt.

@Serializable
data class Score(val player: String = "", val points: Double = 0.0, val isWinner: Boolean = false)

class DatabaseFormatTest {

    // Initializes firebase-java-sdk's android.util.Log on the JVM, through which unknown properties are logged
    @BeforeTest
    fun initializePlatform() {
        assertNotNull(context)
    }

    @Test
    fun writesPropertiesByTheirGetterNames() {
        assertPlain(mapOf("player" to "a", "points" to 1.5, "winner" to true), database.encodeToPlainValue(Score("a", 1.5, true)))
        assertEquals(Score("a", 2.0, true), database.decodeFromPlainValue(hashMapOf("player" to "a", "points" to 2L, "winner" to true)))
    }

    @Test
    fun mapsEnumsByName() {
        assertPlain(mapOf("direction" to "SOUTH"), database.encodeToPlainValue(WithEnum(Direction.SOUTH)))
        assertFailsWithMessage<DatabaseException>("Could not find enum value of com.google.firebase.serialization.Direction for value \"south\"") {
            database.decodeFromPlainValue<WithEnum>(hashMapOf("direction" to "south"))
        }
    }

    @Test
    fun rejectsTheValuesTheAndroidSdkRejectsWithoutAPath() {
        assertFailsWithMessage<DatabaseException>("Numbers of type Short are not supported, please use an int, long, float or double") {
            database.encodeToPlainValue(WithShort(1))
        }
        assertFailsWithMessage<DatabaseException>("Characters are not supported, please use Strings") { database.encodeToPlainValue(WithChar()) }
        assertFailsWithMessage<DatabaseException>("Serializing Collections is not supported, please use Lists instead") {
            database.convertToPlainValue(mapOf("a" to setOf(1)))
        }
        assertFailsWithMessage<DatabaseException>("Maps with non-string keys are not supported") { database.encodeToPlainValue(WithIntKeys(mapOf(1 to "a"))) }
        assertFailsWithMessage<DatabaseException>("Converting to Arrays is not supported, please use Listsinstead") {
            database.decodeFromPlainValue<WithArray>(hashMapOf("tags" to arrayListOf("a")))
        }
        assertFailsWithMessage<DatabaseException>("Expected a List while deserializing, but got a class java.lang.String") {
            database.decodeFromPlainValue<Nested>(hashMapOf("cities" to "LA"))
        }
        assertFailsWithMessage<DatabaseException>("No setter/field for unknown found on class com.google.firebase.serialization.Strict") {
            database.decodeFromPlainValue<Strict>(hashMapOf("unknown" to 1L))
        }
    }

    @Test
    fun hasNoFirestoreTypes() {
        // A Database format passes nothing through: a GeoPoint is only a Firestore type
        val exception = kotlin.runCatching { database.convertToPlainValue(GeoPoint(1.0, 2.0)) }.exceptionOrNull()
        assertEquals(DatabaseException::class, exception!!::class)
        assertPlain(mapOf("title" to "t", "id" to "", "created" to null), database.encodeToPlainValue(Post(title = "t")))
    }
}

class FunctionsFormatTest {

    @Test
    fun acceptsWhatTheAndroidSdkAcceptsAsCallData() {
        val data = mapOf("a" to listOf(1, 2L, 3.5, 4.5f), "b" to mapOf("c" to null, "d" to true), "e" to "s")
        assertPlain(data, functions.convertToPlainValue(data))
        assertPlain(1, functions.convertToPlainValue(1.toShort()))
    }

    @Test
    fun rejectsWhatTheAndroidSdkRejects() {
        assertFailsWithMessage<IllegalArgumentException>("Object cannot be encoded in JSON: c") { functions.convertToPlainValue('c') }
        assertFailsWithMessage<IllegalArgumentException>("Object cannot be encoded in JSON: NORTH") { functions.convertToPlainValue(Direction.NORTH) }
        assertFailsWithMessage<IllegalArgumentException>("Object cannot be encoded in JSON: [1]") { functions.convertToPlainValue(setOf(1)) }
        assertFailsWithMessage<IllegalArgumentException>("Object keys must be strings.") { functions.convertToPlainValue(mapOf(1 to 2)) }
        assertFailsWithMessage<IllegalArgumentException>("Object cannot be encoded in JSON: ${Score()}") { functions.convertToPlainValue(Score()) }
    }

    @Test
    fun mapsClassesThroughTheirSerializerWithTheirSerialNames() {
        assertPlain(mapOf("player" to "a", "points" to 1.5, "isWinner" to true), functions.encodeToPlainValue(Score("a", 1.5, true)))
        assertEquals(Score("a", 1.0), functions.decodeFromPlainValue(hashMapOf("player" to "a", "points" to 1, "unknown" to 2)))
        assertPlain(mapOf("direction" to "SOUTH"), functions.encodeToPlainValue(WithEnum(Direction.SOUTH)))
        assertPlain(mapOf("count" to 1), functions.encodeToPlainValue(WithShort(1)))
    }
}
