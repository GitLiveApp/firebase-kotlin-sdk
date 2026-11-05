/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.firestore.LOAD_BUNDLE_BUFFER_ANDROID_ONLY
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.externals.json
import dev.gitlive.firebase.firestore.externals.clearIndexedDbPersistence
import dev.gitlive.firebase.firestore.externals.connectFirestoreEmulator
import dev.gitlive.firebase.firestore.externals.deleteAllPersistentCacheIndexes
import dev.gitlive.firebase.firestore.externals.disableNetwork
import dev.gitlive.firebase.firestore.externals.disablePersistentCacheIndexAutoCreation
import dev.gitlive.firebase.firestore.externals.doc
import dev.gitlive.firebase.firestore.externals.enableNetwork
import dev.gitlive.firebase.firestore.externals.enablePersistentCacheIndexAutoCreation
import dev.gitlive.firebase.firestore.externals.getFirestore
import dev.gitlive.firebase.firestore.externals.getPersistentCacheIndexManager
import dev.gitlive.firebase.firestore.externals.initializeFirestore
import dev.gitlive.firebase.firestore.externals.memoryEagerGarbageCollector
import dev.gitlive.firebase.firestore.externals.memoryLocalCache
import dev.gitlive.firebase.firestore.externals.memoryLruGarbageCollector
import dev.gitlive.firebase.firestore.externals.namedQuery
import dev.gitlive.firebase.firestore.externals.onSnapshotsInSync
import dev.gitlive.firebase.firestore.externals.persistentLocalCache
import dev.gitlive.firebase.firestore.externals.setLogLevel
import dev.gitlive.firebase.firestore.externals.terminate
import dev.gitlive.firebase.firestore.externals.waitForPendingWrites
import dev.gitlive.firebase.firestore.externals.writeBatch
import kotlinx.coroutines.Runnable
import kotlin.js.Promise
import dev.gitlive.firebase.externals.FirebaseApp as JsFirebaseApp
import dev.gitlive.firebase.firestore.externals.Firestore as JsFirestore
import dev.gitlive.firebase.firestore.externals.PersistentCacheIndexManager as JsPersistentCacheIndexManager
import dev.gitlive.firebase.firestore.externals.collection as jsCollection
import dev.gitlive.firebase.firestore.externals.collectionGroup as jsCollectionGroup
import dev.gitlive.firebase.firestore.externals.loadBundle as jsLoadBundle
import dev.gitlive.firebase.firestore.externals.runTransaction as jsRunTransaction
import dev.gitlive.firebase.firestore.externals.LoadBundleTask as JsLoadBundleTask
import org.khronos.webgl.Uint8Array

private const val DEFAULT_DATABASE = "(default)"

/**
 * The JS SDK's `Firestore` must be initialised with its settings (and connected to the emulator) before any use, so the
 * underlying object is created on first use with the settings set until then, as the Android SDK's semantics require.
 * Instances are cached per app and database, as the Android SDK's `getInstance`.
 *
 * @property js The underlying Firebase JS SDK object.
 */
public actual class FirebaseFirestore private constructor(private val jsApp: JsFirebaseApp, private val databaseId: String?, existing: JsFirestore?) {
    private var settings: FirebaseFirestoreSettings? = null
    private var emulator: Pair<String, Int>? = null

    private val lazyJs = lazy {
        rethrow {
            (existing ?: settings?.let { initializeFirestore(jsApp, it.toJs(), databaseId) } ?: getFirestore(jsApp, databaseId))
                .also { js ->
                    emulator?.let { (host, port) -> connectFirestoreEmulator(js, host, port) }
                    Instances.byJs += js to this
                }
        }
    }

    public val js: JsFirestore by lazyJs

    public actual val app: FirebaseApp get() = FirebaseApp.getInstance(jsApp.name)

    public actual var firestoreSettings: FirebaseFirestoreSettings
        get() = settings ?: FirebaseFirestoreSettings.Builder().build()
        set(value) {
            check(!lazyJs.isInitialized() || value == settings) { "FirebaseFirestore has already been started and its settings can no longer be changed. You can only call setFirestoreSettings() before calling any other methods on a FirebaseFirestore object." }
            settings = value
        }

    public actual val persistentCacheIndexManager: PersistentCacheIndexManager?
        get() = rethrow { getPersistentCacheIndexManager(js)?.let { PersistentCacheIndexManager(it) } }

    public actual fun collection(collectionPath: String): CollectionReference = rethrow { CollectionReference(jsCollection(js, collectionPath)) }

    public actual fun collectionGroup(collectionId: String): Query = rethrow { Query(jsCollectionGroup(js, collectionId)) }

    public actual fun document(documentPath: String): DocumentReference = rethrow { DocumentReference(doc(js, documentPath)) }

    public actual fun batch(): WriteBatch = rethrow { WriteBatch(writeBatch(js)) }

    public actual fun runBatch(batchFunction: WriteBatch.Function): Task<Nothing?> = write {
        val batch = batch()
        batchFunction.apply(batch)
        batch.js.commit()
    }

    public actual fun <TResult> runTransaction(updateFunction: Transaction.Function<TResult>): Task<TResult> = runTransaction(null, updateFunction)

    public actual fun <TResult> runTransaction(options: TransactionOptions, updateFunction: Transaction.Function<TResult>): Task<TResult> = runTransaction(json("maxAttempts" to options.maxAttempts), updateFunction)

    /** The result stays on the Kotlin side (a Kotlin value cannot travel through a JS promise): the SDK's promise only carries the outcome. */
    private fun <TResult> runTransaction(options: JsAny?, updateFunction: Transaction.Function<TResult>): Task<TResult> {
        val source = TaskCompletionSource<TResult>()
        var result: TResult? = null
        var failure: Throwable? = null
        try {
            jsRunTransaction(
                js,
                { transaction ->
                    try {
                        result = updateFunction.apply(Transaction(transaction))
                        Promise.resolve<JsAny?>(null)
                    } catch (e: Throwable) {
                        failure = e
                        Promise.reject(jsError(e.message ?: e.toString()))
                    }
                },
                options,
            ).then(
                onFulfilled = {
                    source.setResult(result)
                    null
                },
                onRejected = { error ->
                    source.setException(failure?.let { it as? Exception ?: FirebaseFirestoreException(it.message ?: it.toString(), FirebaseFirestoreException.Code.ABORTED, it) } ?: error.toFirestoreException())
                    null
                },
            )
        } catch (e: Throwable) {
            source.setException(e.toFirestoreException())
        }
        return source.task
    }

    /** Runs a suspending transaction body, for the dev.gitlive layer; the JS SDK takes a promise-returning function. */
    internal fun runPromiseTransaction(options: TransactionOptions?, updateFunction: (Transaction) -> Promise<JsAny?>): Promise<JsAny?> = rethrow {
        jsRunTransaction(js, { transaction -> updateFunction(Transaction(transaction)) }, options?.let { json("maxAttempts" to it.maxAttempts) })
    }

    public actual fun addSnapshotsInSyncListener(runnable: Runnable): ListenerRegistration {
        val unsubscribe = rethrow { onSnapshotsInSync(js) { runnable.run() } }
        return ListenerRegistration { rethrow { unsubscribe() } }
    }

    public actual fun loadBundle(bundleData: ByteArray): LoadBundleTask = loadBundle { jsLoadBundle(js, bundleData.toUint8Array().buffer) }

    public actual fun loadBundle(bundleData: java.io.InputStream): LoadBundleTask = loadBundle { jsLoadBundle(js, bundleData.toReadableStream()) }

    /** The task of the JS SDK's bundle load [start] returns, as a [LoadBundleTask]; a load that fails to start fails the task. */
    private fun loadBundle(start: () -> JsLoadBundleTask): LoadBundleTask {
        val task = LoadBundleTaskImpl()
        try {
            val jsTask = start()
            jsTask.onProgress({ task.update(it.toProgress()) }, {}, {})
            jsTask.then({ task.update(it.toProgress()) }, { task.fail(it.toFirestoreException()) })
        } catch (e: Throwable) {
            task.fail(e.toFirestoreException())
        }
        return task
    }

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(LOAD_BUNDLE_BUFFER_ANDROID_ONLY, ReplaceWith("loadBundle(bundleData.array())"), DeprecationLevel.ERROR)
    public actual fun loadBundle(bundleData: java.nio.ByteBuffer): LoadBundleTask = throw UnsupportedOperationException(LOAD_BUNDLE_BUFFER_ANDROID_ONLY)

    public actual fun getNamedQuery(name: String): Task<Query?> = task({ namedQuery(js, name) }) { query -> query?.let { Query(it) } }

    public actual fun useEmulator(host: String, port: Int) {
        emulator = host to port
        if (lazyJs.isInitialized()) rethrow { connectFirestoreEmulator(js, host, port) }
    }

    public actual fun clearPersistence(): Task<Nothing?> = write { clearIndexedDbPersistence(js) }

    public actual fun disableNetwork(): Task<Nothing?> = write { disableNetwork(js) }

    public actual fun enableNetwork(): Task<Nothing?> = write { enableNetwork(js) }

    public actual fun terminate(): Task<Nothing?> {
        Instances.remove(this)
        return write { terminate(js) }
    }

    public actual fun waitForPendingWrites(): Task<Nothing?> = write { waitForPendingWrites(js) }

    override fun toString(): String = "FirebaseFirestore(app=${jsApp.name}, database=${databaseId ?: DEFAULT_DATABASE})"

    // JS objects are compared by reference, so the instances are kept in lists rather than keyed by them.
    private object Instances {
        val byApp = mutableMapOf<String, MutableMap<String, FirebaseFirestore>>()
        val byJs = mutableListOf<Pair<JsFirestore, FirebaseFirestore>>()

        fun of(app: JsFirebaseApp, databaseId: String?): FirebaseFirestore = byApp.getOrPut(app.name) { mutableMapOf() }.getOrPut(databaseId ?: DEFAULT_DATABASE) { FirebaseFirestore(app, databaseId, null) }

        fun of(js: JsFirestore): FirebaseFirestore = byJs.firstOrNull { it.first == js }?.second ?: FirebaseFirestore(js.app, null, js).also { byJs += js to it }

        fun remove(instance: FirebaseFirestore) {
            byApp[instance.jsApp.name]?.values?.remove(instance)
            byJs.removeAll { it.second == instance }
        }
    }

    public actual companion object {
        public actual fun getInstance(): FirebaseFirestore = getInstance(FirebaseApp.getInstance())

        public actual fun getInstance(app: FirebaseApp): FirebaseFirestore = Instances.of(app.js, null)

        public actual fun getInstance(app: FirebaseApp, database: String): FirebaseFirestore = Instances.of(app.js, database)

        public actual fun getInstance(database: String): FirebaseFirestore = getInstance(FirebaseApp.getInstance(), database)

        public actual fun setLoggingEnabled(loggingEnabled: Boolean) {
            rethrow { setLogLevel(if (loggingEnabled) "debug" else "silent") }
        }

        /** The instance wrapping [js], which a reference or query belongs to. */
        internal fun wrap(js: JsFirestore): FirebaseFirestore = Instances.of(js)
    }
}

private fun jsError(message: String): JsAny = js("new Error(message)")

private fun FirebaseFirestoreSettings.toJs(): JsAny {
    val localCache = when (val cache = cacheSettings) {
        is PersistentCacheSettings -> persistentLocalCache(json("cacheSizeBytes" to cache.sizeBytes))
        is MemoryCacheSettings -> memoryLocalCache(
            json(
                "garbageCollector" to when (val gc = cache.garbageCollectorSettings) {
                    is MemoryLruGcSettings -> memoryLruGarbageCollector(json("cacheSizeBytes" to gc.sizeBytes))
                    else -> memoryEagerGarbageCollector()
                },
            ),
        )
        else -> null
    }
    return if (localCache == null) json("host" to host, "ssl" to isSslEnabled) else json("host" to host, "ssl" to isSslEnabled, "localCache" to localCache)
}

private fun dev.gitlive.firebase.firestore.externals.LoadBundleTaskProgress.toProgress(): LoadBundleTaskProgress = LoadBundleTaskProgress(
    documentsLoaded,
    totalDocuments,
    bytesLoaded.toLong(),
    totalBytes.toLong(),
    when (taskState) {
        "Success" -> LoadBundleTaskProgress.TaskState.SUCCESS
        "Error" -> LoadBundleTaskProgress.TaskState.ERROR
        else -> LoadBundleTaskProgress.TaskState.RUNNING
    },
    null,
)

/** @property js The underlying Firebase JS SDK object. */
public actual class PersistentCacheIndexManager internal constructor(public val js: JsPersistentCacheIndexManager) {
    public actual fun enableIndexAutoCreation() {
        rethrow { enablePersistentCacheIndexAutoCreation(js) }
    }

    public actual fun disableIndexAutoCreation() {
        rethrow { disablePersistentCacheIndexAutoCreation(js) }
    }

    public actual fun deleteAllIndexes() {
        rethrow { deleteAllPersistentCacheIndexes(js) }
    }
}

/**
 * A `ReadableStream` the JS SDK pulls the stream's bytes through, chunk by chunk, as it parses the bundle; the stream is
 * closed at its end, and a failing read errors the `ReadableStream`, which fails the load.
 */
private fun java.io.InputStream.toReadableStream(): JsAny = readableStream { controller ->
    try {
        val buffer = ByteArray(BUNDLE_CHUNK_SIZE)
        val count = read(buffer)
        if (count < 0) {
            close()
            closeStream(controller)
        } else {
            enqueue(controller, buffer.copyOf(count).toUint8Array())
        }
    } catch (e: Throwable) {
        errorStream(controller, jsError(e.message ?: e.toString()))
    }
}

private const val BUNDLE_CHUNK_SIZE = 16 * 1024

private fun readableStream(pull: (JsAny) -> Unit): JsAny = js("new ReadableStream({ pull: (controller) => pull(controller) })")

private fun enqueue(controller: JsAny, chunk: Uint8Array): Unit = js("controller.enqueue(chunk)")

private fun closeStream(controller: JsAny): Unit = js("controller.close()")

private fun errorStream(controller: JsAny, error: JsAny): Unit = js("controller.error(error)")
