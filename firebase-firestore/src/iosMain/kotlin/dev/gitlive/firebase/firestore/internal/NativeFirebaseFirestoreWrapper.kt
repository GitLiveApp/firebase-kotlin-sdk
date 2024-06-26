package dev.gitlive.firebase.firestore.internal

import cocoapods.FirebaseFirestoreInternal.FIRFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreSettings
import dev.gitlive.firebase.firestore.NativeFirebaseFirestore
import dev.gitlive.firebase.firestore.NativeTransaction
import dev.gitlive.firebase.firestore.await
import dev.gitlive.firebase.firestore.awaitResult
import dev.gitlive.firebase.firestore.firestoreSettings
import dev.gitlive.firebase.firestore.memoryCacheSettings
import kotlinx.coroutines.runBlocking

@Suppress("UNCHECKED_CAST")
internal actual class NativeFirebaseFirestoreWrapper internal actual constructor(actual val native: NativeFirebaseFirestore) {

    actual var settings: FirebaseFirestoreSettings = firestoreSettings { }.also {
        native.settings = it.ios
    }
        set(value) {
            field = value
            native.settings = value.ios
        }

    actual fun collection(collectionPath: String) = native.collectionWithPath(collectionPath)

    actual fun collectionGroup(collectionId: String) = native.collectionGroupWithID(collectionId)

    actual fun document(documentPath: String) =
        NativeDocumentReference(native.documentWithPath(documentPath))

    actual fun batch() = native.batch()

    actual fun setLoggingEnabled(loggingEnabled: Boolean): Unit =
        FIRFirestore.enableLogging(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend NativeTransaction.() -> T) =
        awaitResult<Any?> {
            native.runTransactionWithBlock(
                { transaction, _ -> runBlocking { transaction!!.func() } },
                it,
            )
        } as T

    actual suspend fun clearPersistence() =
        await { native.clearPersistenceWithCompletion(it) }

    actual fun useEmulator(host: String, port: Int) {
        native.useEmulatorWithHost(host, port.toLong())
        settings = firestoreSettings(settings) {
            this.host = "$host:$port"
            cacheSettings = memoryCacheSettings { }
            sslEnabled = false
        }
    }

    actual suspend fun disableNetwork() {
        await { native.disableNetworkWithCompletion(it) }
    }

    actual suspend fun enableNetwork() {
        await { native.enableNetworkWithCompletion(it) }
    }
}
