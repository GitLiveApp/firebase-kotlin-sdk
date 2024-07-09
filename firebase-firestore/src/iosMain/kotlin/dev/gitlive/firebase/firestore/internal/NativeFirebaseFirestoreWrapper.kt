package dev.gitlive.firebase.firestore.internal

import cocoapods.FirebaseFirestoreInternal.FIRFirestore
import cocoapods.FirebaseFirestoreInternal.FIRMemoryCacheSettings
import cocoapods.FirebaseFirestoreInternal.FIRPersistentCacheSettings
import dev.gitlive.firebase.firestore.FirebaseFirestoreSettings
import dev.gitlive.firebase.firestore.LocalCacheSettings
import dev.gitlive.firebase.firestore.MemoryGarbageCollectorSettings
import dev.gitlive.firebase.firestore.NativeFirebaseFirestore
import dev.gitlive.firebase.firestore.NativeTransaction
import dev.gitlive.firebase.firestore.await
import dev.gitlive.firebase.firestore.awaitResult
import dev.gitlive.firebase.firestore.firestoreSettings
import dev.gitlive.firebase.firestore.memoryCacheSettings
import dev.gitlive.firebase.firestore.persistentCacheSettings
import kotlinx.coroutines.runBlocking

@Suppress("UNCHECKED_CAST")
internal actual class NativeFirebaseFirestoreWrapper internal actual constructor(actual val native: NativeFirebaseFirestore) {

    actual var settings: FirebaseFirestoreSettings
        get() = firestoreSettings {
            host = native.settings.host
            sslEnabled = native.settings.sslEnabled
            dispatchQueue = native.settings.dispatchQueue
            @Suppress("SENSELESS_NULL_IN_WHEN")
            cacheSettings = when (val nativeCacheSettings = native.settings.cacheSettings) {
                is FIRPersistentCacheSettings -> persistentCacheSettings {
                    // SizeBytes cannot be determined
                }
                is FIRMemoryCacheSettings -> memoryCacheSettings {
                    // Garbage collection settings cannot be determined
                }
                null -> when {
                    native.settings.persistenceEnabled -> LocalCacheSettings.Persistent(native.settings.cacheSizeBytes)
                    native.settings.cacheSizeBytes == FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED -> LocalCacheSettings.Memory(
                        MemoryGarbageCollectorSettings.Eager,
                    )
                    else -> LocalCacheSettings.Memory(
                        MemoryGarbageCollectorSettings.LRUGC(
                            native.settings.cacheSizeBytes,
                        ),
                    )
                }
                else -> error("Unknown cache settings $nativeCacheSettings")
            }
        }
        set(value) {
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
