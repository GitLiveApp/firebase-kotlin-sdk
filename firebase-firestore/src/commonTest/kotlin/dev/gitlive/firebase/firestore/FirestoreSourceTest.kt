package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlin.test.*

/**
 * These tests are separated from other tests because
 * testing Firestore Source requires toggling persistence settings per test.
 */
class FirestoreSourceTest {
    lateinit var firestore: FirebaseFirestore

    companion object {
        val testDoc = FirebaseFirestoreTest.FirestoreTest(
            "aaa",
            0.0,
            1,
            listOf("a", "aa", "aaa"),
            "notNull",
        )
    }

    private suspend fun setDoc() {
        firestore.collection("testFirestoreQuerying").document("one").set(testDoc)
    }

    private fun initializeFirebase(persistenceEnabled: Boolean = false) {
        val app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(
            context,
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        )

        firestore = Firebase.firestore(app).apply {
            useEmulator(emulatorHost, 8080)
            settings = firestoreSettings(settings) {
                cacheSettings = if (persistenceEnabled) {
                    persistentCacheSettings { }
                } else {
                    memoryCacheSettings { }
                }
            }
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun testGetFromServer_withPersistence() = runTest {
        initializeFirebase(persistenceEnabled = true)
        setDoc()
        val doc = firestore.collection("testFirestoreQuerying").document("one").get(Source.SERVER)
        assertTrue(doc.exists)
        assertFalse(doc.metadata.isFromCache)
    }

    @Test
    fun testGetFromServer_withoutPersistence() = runTest {
        initializeFirebase(persistenceEnabled = false)
        setDoc()
        val doc = firestore.collection("testFirestoreQuerying").document("one").get(Source.SERVER)
        assertTrue(doc.exists)
        assertFalse(doc.metadata.isFromCache)
    }

    @Test
    fun testGetFromCache() = runTest {
        initializeFirebase(persistenceEnabled = true)

        // Warm up cache by setting a document
        setDoc()

        val cachedDoc = firestore.collection("testFirestoreQuerying").document("one").get(Source.CACHE)
        assertTrue(cachedDoc.exists)
        assertTrue(cachedDoc.metadata.isFromCache)
    }

    @Test
    fun testGetFromCache_withoutPersistence() = runTest {
        initializeFirebase(persistenceEnabled = false)
        setDoc()
        assertFailsWith(FirebaseFirestoreException::class) {
            firestore.collection("testFirestoreQuerying").document("one").get(Source.CACHE)
        }
    }

    @Test
    fun testGetDefault_withPersistence() = runTest {
        initializeFirebase(persistenceEnabled = false)
        val doc = firestore.collection("testFirestoreQuerying").document("one").get(Source.DEFAULT)
        assertTrue(doc.exists)
        assertFalse(doc.metadata.isFromCache)
    }

    @Test
    fun testGet() = runTest {
        initializeFirebase(persistenceEnabled = false)
        val doc = firestore.collection("testFirestoreQuerying").document("one").get()
        assertTrue(doc.exists)
        assertFalse(doc.metadata.isFromCache)
    }

    @Test
    fun testGetDefault_withoutPersistence() = runTest {
        initializeFirebase(persistenceEnabled = true)
        setDoc()
        val doc = firestore.collection("testFirestoreQuerying").document("one").get(Source.DEFAULT)
        assertTrue(doc.exists)
        // Firebase defaults to first fetching from server
        assertFalse(doc.metadata.isFromCache)
    }
}
