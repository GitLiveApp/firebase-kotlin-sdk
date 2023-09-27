package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import kotlinx.serialization.Serializable
import kotlin.test.BeforeTest
import kotlin.test.Test

@IgnoreJs
@IgnoreForAndroidUnitTest
class FirestoreAsync {

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
                        projectId = "fir-kotlin-sdk",
                        gcmSenderId = "846484016111"
                    )
                )
                Firebase.firestore.useEmulator(emulatorHost, 8080)
                Firebase.firestore.setSettings(createFirestoreTestSettings(cacheSettings = LocalCacheSettings.Persistent()))
            }
    }
    @Serializable
    data class TestData(val value: Int)

    @Test
    fun asyncDocumentReferenceTest() = runTest {
        fun getDocument() = Firebase.firestore.collection("asyncDocumentReferenceTest")
            .document("asyncDocumentReferenceTest")

        Firebase.firestore.disableNetwork()
        val update1 = getDocument().async.set(TestData(1))
        val update2 = getDocument().async.update(TestData(2))
        Firebase.firestore.enableNetwork()
        update1.await()
        update2.await()
    }

    @Test
    fun asyncBatchTest() = runTest {
        val batch = Firebase.firestore.batch()

        fun getDocument() = Firebase.firestore.collection("asyncBatchTest")
            .document("asyncBatchTest")

        Firebase.firestore.disableNetwork()
        batch.set(getDocument(), TestData(1))
        batch.update(getDocument(), TestData(1))

        val result = batch.async.commit()
        Firebase.firestore.enableNetwork()

        result.await()
    }

    @Test
    fun asyncCollectionAddTest() = runTest {
        Firebase.firestore.disableNetwork()
        val result = Firebase.firestore.collection("asyncCollectionAddTest").async
            .add(TestData(1))
        Firebase.firestore.enableNetwork()

        result.await()
    }
}
