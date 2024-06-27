package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.externals.FirebaseApp
import dev.gitlive.firebase.firestore.FirebaseFirestoreSettings
import dev.gitlive.firebase.firestore.NativeCollectionReference
import dev.gitlive.firebase.firestore.NativeFirebaseFirestore
import dev.gitlive.firebase.firestore.NativeQuery
import dev.gitlive.firebase.firestore.NativeTransaction
import dev.gitlive.firebase.firestore.NativeWriteBatch
import dev.gitlive.firebase.firestore.externals.clearIndexedDbPersistence
import dev.gitlive.firebase.firestore.externals.connectFirestoreEmulator
import dev.gitlive.firebase.firestore.externals.doc
import dev.gitlive.firebase.firestore.externals.initializeFirestore
import dev.gitlive.firebase.firestore.externals.setLogLevel
import dev.gitlive.firebase.firestore.externals.writeBatch
import dev.gitlive.firebase.firestore.firestoreSettings
import dev.gitlive.firebase.firestore.rethrow
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise

internal actual class NativeFirebaseFirestoreWrapper internal constructor(
    private val createNative: NativeFirebaseFirestoreWrapper.() -> NativeFirebaseFirestore,
) {

    internal actual constructor(native: NativeFirebaseFirestore) : this({ native })
    internal constructor(app: FirebaseApp) : this(
        {
            NativeFirebaseFirestore(
                initializeFirestore(app, settings.js).also {
                    emulatorSettings?.run {
                        connectFirestoreEmulator(it, host, port)
                    }
                },
            )
        },
    )

    private data class EmulatorSettings(val host: String, val port: Int)

    actual var settings: FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder().build()
        set(value) {
            if (lazyNative.isInitialized()) {
                throw IllegalStateException("FirebaseFirestore has already been started and its settings can no longer be changed. You can only call setFirestoreSettings() before calling any other methods on a FirebaseFirestore object.")
            } else {
                field = value
            }
        }
    private var emulatorSettings: EmulatorSettings? = null

    // initializeFirestore must be called before any call, including before `getFirestore()`
    // To allow settings to be updated, we defer creating the wrapper until the first call to `native`
    private val lazyNative = lazy {
        createNative()
    }
    actual val native: NativeFirebaseFirestore by lazyNative
    private val js get() = native.js

    actual fun collection(collectionPath: String) = rethrow {
        NativeCollectionReference(
            dev.gitlive.firebase.firestore.externals.collection(
                js,
                collectionPath,
            ),
        )
    }

    actual fun collectionGroup(collectionId: String) = rethrow {
        NativeQuery(
            dev.gitlive.firebase.firestore.externals.collectionGroup(
                js,
                collectionId,
            ),
        )
    }

    actual fun document(documentPath: String) = rethrow {
        NativeDocumentReference(
            doc(
                js,
                documentPath,
            ),
        )
    }

    actual fun batch() = rethrow { NativeWriteBatch(writeBatch(js)) }

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        rethrow { setLogLevel(if (loggingEnabled) "error" else "silent") }

    @OptIn(DelicateCoroutinesApi::class)
    actual suspend fun <T> runTransaction(func: suspend NativeTransaction.() -> T) =
        rethrow {
            dev.gitlive.firebase.firestore.externals.runTransaction(
                js,
                { GlobalScope.promise { NativeTransaction(it).func() } },
            ).await()
        }

    actual suspend fun clearPersistence() =
        rethrow { clearIndexedDbPersistence(js).await() }

    actual fun useEmulator(host: String, port: Int) = rethrow {
        settings = firestoreSettings(settings) {
            this.host = "$host:$port"
        }
        emulatorSettings = EmulatorSettings(host, port)
    }

    actual suspend fun disableNetwork() {
        rethrow { dev.gitlive.firebase.firestore.externals.disableNetwork(js).await() }
    }

    actual suspend fun enableNetwork() {
        rethrow { dev.gitlive.firebase.firestore.externals.enableNetwork(js).await() }
    }
}
