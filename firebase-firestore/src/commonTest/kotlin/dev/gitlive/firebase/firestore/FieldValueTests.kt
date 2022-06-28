package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firebaseSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FieldValueTests {
    @Test
    fun equalityChecks() = runTest {
        assertEquals(FieldValue.delete, FieldValue.delete)
        assertEquals(FieldValue.serverTimestamp, FieldValue.serverTimestamp)
        assertNotEquals(FieldValue.delete, FieldValue.serverTimestamp)

        // Note: arrayUnion and arrayRemove can't be checked due to vararg to array conversion
//        assertEquals(FieldValue.arrayUnion(1, 2, 3), FieldValue.arrayUnion(1, 2, 3))
//        assertNotEquals(FieldValue.arrayUnion(1, 2, 3), FieldValue.arrayUnion(1, 2, 3, 4))
//
//        assertEquals(FieldValue.arrayRemove(1, 2, 3), FieldValue.arrayRemove(1, 2, 3, 4))
//        assertNotEquals(FieldValue.arrayRemove(1, 2, 3), FieldValue.arrayRemove(1, 2, 3, 4))
//
//        assertNotEquals(FieldValue.arrayUnion(1, 2, 3), FieldValue.arrayRemove(1, 2, 3))
    }

    @Test
    @IgnoreJs
    fun serializers() = runTest {
        assertEquals(FieldValueSerializer, FieldValue.delete.firebaseSerializer())
    }
}
