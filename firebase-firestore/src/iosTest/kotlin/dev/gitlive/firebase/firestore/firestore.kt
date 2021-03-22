/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import platform.Foundation.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual fun runTest(test: suspend () -> Unit) = runBlocking {
    val testRun = MainScope().async { test() }
    while (testRun.isActive) {
        NSRunLoop.mainRunLoop.runMode(
            NSDefaultRunLoopMode,
            beforeDate = NSDate.create(timeInterval = 1.0, sinceDate = NSDate())
        )
        yield()
    }
    testRun.await()
}

class FirebaseFirestoreIOSTest {

    @BeforeTest
    fun initializeFirebase() {
        Firebase
            .takeIf { Firebase.apps(context).isEmpty() }
            ?.apply {
                initialize(
                    context,
                    FirebaseOptions(
                        applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                        apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                        databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                        storageBucket = "fir-kotlin-sdk.appspot.com",
                        projectId = "fir-kotlin-sdk"
                    )
                )
                Firebase.firestore.useEmulator(emulatorHost, 8080)
            }
    }

    @Serializable
    data class TestDataWithDocumentReference(
        val uid: String,
        @Serializable(with = FirebaseDocumentReferenceSerializer::class)
        val reference: DocumentReference,
        @Serializable(with = FirebaseReferenceNullableSerializer::class)
        val ref: FirebaseReference?
    )

    @Test
    fun encodeDocumentReferenceObject() = runTest {
        val doc = Firebase.firestore.document("a/b")
        val item = TestDataWithDocumentReference("123", doc, FirebaseReference.Value(doc))
        val encoded = encode(item, shouldEncodeElementDefault = false) as Map<String, Any?>
        assertEquals("123", encoded["uid"])
        assertEquals(doc.ios, encoded["reference"])
        assertEquals(doc.ios, encoded["ref"])
    }

    @Test
    fun encodeDeleteDocumentReferenceObject() = runTest {
        val doc = Firebase.firestore.document("a/b")
        val item = TestDataWithDocumentReference("123", doc, FirebaseReference.ServerDelete)
        val encoded = encode(item, shouldEncodeElementDefault = false) as Map<String, Any?>
        assertEquals("123", encoded["uid"])
        assertEquals(doc.ios, encoded["reference"])
        assertEquals(FieldValue.delete, encoded["ref"])
    }

    @Test
    fun decodeDocumentReferenceObject() = runTest {
        val doc = Firebase.firestore.document("a/b")
        val obj = mapOf("uid" to "123", "reference" to doc.ios, "ref" to doc.ios)
        val decoded: TestDataWithDocumentReference = decode(obj)
        assertEquals("123", decoded.uid)
        assertEquals(doc.path, decoded.reference.path)
        assertEquals(doc.path, decoded.ref?.reference?.path)
    }
}
