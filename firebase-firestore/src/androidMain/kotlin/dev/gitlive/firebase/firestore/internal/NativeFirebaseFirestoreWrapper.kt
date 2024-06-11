package dev.gitlive.firebase.firestore.internal

import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.MemoryEagerGcSettings
import com.google.firebase.firestore.MemoryLruGcSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.firestoreSettings
import dev.gitlive.firebase.firestore.FirebaseFirestoreSettings
import dev.gitlive.firebase.firestore.LocalCacheSettings
import dev.gitlive.firebase.firestore.MemoryGarbageCollectorSettings
import dev.gitlive.firebase.firestore.NativeFirebaseFirestore
import dev.gitlive.firebase.firestore.NativeTransaction
import dev.gitlive.firebase.firestore.android
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

internal actual class NativeFirebaseFirestoreWrapper actual constructor(actual val native: NativeFirebaseFirestore) {

    actual var settings: FirebaseFirestoreSettings
        get() = with(native.firestoreSettings) {
            FirebaseFirestoreSettings(
                isSslEnabled,
                host,
                cacheSettings?.let { localCacheSettings ->
                    when (localCacheSettings) {
                        is MemoryCacheSettings -> {
                            val garbageCollectionSettings =
                                when (val settings = localCacheSettings.garbageCollectorSettings) {
                                    is MemoryEagerGcSettings -> MemoryGarbageCollectorSettings.Eager
                                    is MemoryLruGcSettings -> MemoryGarbageCollectorSettings.LRUGC(
                                        settings.sizeBytes,
                                    )

                                    else -> throw IllegalArgumentException("Existing settings does not have valid GarbageCollectionSettings")
                                }
                            LocalCacheSettings.Memory(garbageCollectionSettings)
                        }

                        is PersistentCacheSettings -> LocalCacheSettings.Persistent(
                            localCacheSettings.sizeBytes,
                        )

                        else -> throw IllegalArgumentException("Existing settings is not of a valid type")
                    }
                } ?: kotlin.run {
                    @Suppress("DEPRECATION")
                    when {
                        isPersistenceEnabled -> LocalCacheSettings.Persistent(cacheSizeBytes)
                        cacheSizeBytes == FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED -> LocalCacheSettings.Memory(
                            MemoryGarbageCollectorSettings.Eager,
                        )

                        else -> LocalCacheSettings.Memory(
                            MemoryGarbageCollectorSettings.LRUGC(
                                cacheSizeBytes,
                            ),
                        )
                    }
                },
                callbackExecutorMap[native] ?: TaskExecutors.MAIN_THREAD,
            )
        }
        set(value) {
            native.firestoreSettings = firestoreSettings {
                isSslEnabled = value.sslEnabled
                host = value.host
                setLocalCacheSettings(value.cacheSettings.android)
            }
            callbackExecutorMap[native] = value.callbackExecutor
        }

    actual fun collection(collectionPath: String) = native.collection(collectionPath)

    actual fun collectionGroup(collectionId: String) = native.collectionGroup(collectionId)

    actual fun document(documentPath: String) =
        NativeDocumentReference(native.document(documentPath))

    actual fun batch() = native.batch()

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        com.google.firebase.firestore.FirebaseFirestore.setLoggingEnabled(loggingEnabled)

    actual suspend fun <T> runTransaction(func: suspend NativeTransaction.() -> T): T =
        native.runTransaction { runBlocking { it.func() } }.await()

    actual suspend fun clearPersistence() =
        native.clearPersistence().await().run { }

    actual fun useEmulator(host: String, port: Int) {
        native.useEmulator(host, port)
    }

    actual suspend fun disableNetwork() =
        native.disableNetwork().await().run { }

    actual suspend fun enableNetwork() =
        native.enableNetwork().await().run { }
}
