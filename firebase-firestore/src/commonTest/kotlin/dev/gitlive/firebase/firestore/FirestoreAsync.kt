package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.serialization.Serializable
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@IgnoreJs
@IgnoreForAndroidUnitTest
class FirestoreAsync {

    lateinit var firestore: FirebaseFirestore

    @BeforeTest
    fun initializeFirebase() {
        val app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(
            context,
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111"
            )
        )

        firestore = Firebase.firestore(app).apply {
            useEmulator(emulatorHost, 8080)
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Serializable
    data class TestData(val value: Int)

    @Test
    fun asyncDocumentReferenceTest() = runTest {
        fun getDocument() = firestore.collection("asyncDocumentReferenceTest")
            .document("asyncDocumentReferenceTest")

        firestore.disableNetwork()
        val update1 = getDocument().async.set(TestData(1))
        val update2 = getDocument().async.update(TestData(2))
        firestore.enableNetwork()
        update1.await()
        update2.await()
    }

    @Test
    fun asyncBatchTest() = runTest {
        val batch = firestore.batch()

        fun getDocument() = firestore.collection("asyncBatchTest")
            .document("asyncBatchTest")

        firestore.disableNetwork()
        batch.set(getDocument(), TestData(1))
        batch.update(getDocument(), TestData(1))

        val result = batch.async.commit()
        firestore.enableNetwork()

        result.await()
    }

    @Test
    fun asyncCollectionAddTest() = runTest {
        firestore.disableNetwork()
        val result = firestore.collection("asyncCollectionAddTest").async
            .add(TestData(1))
        firestore.enableNetwork()

        result.await()
    }
}
