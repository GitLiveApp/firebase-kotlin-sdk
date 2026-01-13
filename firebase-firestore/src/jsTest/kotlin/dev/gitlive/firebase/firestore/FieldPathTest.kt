package dev.gitlive.firebase.firestore

import kotlin.test.Test
import kotlin.test.assertEquals

class FieldPathTest {

    @Test
    fun testFieldPathString() {
        val fieldPath = FieldPath("field1", "field2", "field3")
        assertEquals("field1.field2.field3", fieldPath.pathString)
    }
}
