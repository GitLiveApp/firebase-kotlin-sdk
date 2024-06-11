/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.externals.getApp
import dev.gitlive.firebase.firestore.externals.MemoryCacheSettings
import dev.gitlive.firebase.firestore.externals.PersistentCacheSettings
import dev.gitlive.firebase.firestore.externals.memoryEagerGarbageCollector
import dev.gitlive.firebase.firestore.externals.memoryLocalCache
import dev.gitlive.firebase.firestore.externals.memoryLruGarbageCollector
import dev.gitlive.firebase.firestore.externals.persistentLocalCache
import dev.gitlive.firebase.firestore.internal.NativeDocumentSnapshotWrapper
import dev.gitlive.firebase.firestore.internal.NativeFirebaseFirestoreWrapper
import kotlin.js.Json
import kotlin.js.json
import dev.gitlive.firebase.firestore.externals.Firestore as JsFirestore
import dev.gitlive.firebase.firestore.externals.CollectionReference as JsCollectionReference
import dev.gitlive.firebase.firestore.externals.DocumentChange as JsDocumentChange
import dev.gitlive.firebase.firestore.externals.DocumentReference as JsDocumentReference
import dev.gitlive.firebase.firestore.externals.DocumentSnapshot as JsDocumentSnapshot
import dev.gitlive.firebase.firestore.externals.FieldPath as JsFieldPath
import dev.gitlive.firebase.firestore.externals.Query as JsQuery
import dev.gitlive.firebase.firestore.externals.QuerySnapshot as JsQuerySnapshot
import dev.gitlive.firebase.firestore.externals.SnapshotMetadata as JsSnapshotMetadata
import dev.gitlive.firebase.firestore.externals.Transaction as JsTransaction
import dev.gitlive.firebase.firestore.externals.WriteBatch as JsWriteBatch
import dev.gitlive.firebase.firestore.externals.documentId as jsDocumentId

actual val Firebase.firestore get() =
    rethrow { FirebaseFirestore(NativeFirebaseFirestoreWrapper(getApp())) }

actual fun Firebase.firestore(app: FirebaseApp) =
    rethrow { FirebaseFirestore(NativeFirebaseFirestoreWrapper(app.js)) }

actual data class NativeFirebaseFirestore(val js: JsFirestore)

val FirebaseFirestore.js: JsFirestore get() = native.js

actual data class FirebaseFirestoreSettings(
    actual val sslEnabled: Boolean,
    actual val host: String,
    actual val cacheSettings: LocalCacheSettings,
) {

    actual companion object {
        actual val CACHE_SIZE_UNLIMITED: Long = -1L
        internal actual val DEFAULT_HOST: String = "firestore.googleapis.com"
        internal actual val MINIMUM_CACHE_BYTES: Long = 1 * 1024 * 1024

        // According to documentation, default JS Firestore cache size is 40MB, not 100MB
        internal actual val DEFAULT_CACHE_SIZE_BYTES: Long = 40 * 1024 * 1024
    }

    actual class Builder internal constructor(
        actual var sslEnabled: Boolean,
        actual var host: String,
        actual var cacheSettings: LocalCacheSettings,
    ) {

        actual constructor() : this(
            true,
            DEFAULT_HOST,
            persistentCacheSettings { },
        )
        actual constructor(settings: FirebaseFirestoreSettings) : this(settings.sslEnabled, settings.host, settings.cacheSettings)

        actual fun build(): FirebaseFirestoreSettings = FirebaseFirestoreSettings(sslEnabled, host, cacheSettings)
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    val js: Json get() = json().apply {
        set("ssl", sslEnabled)
        set("host", host)
        set(
            "localCache",
            when (cacheSettings) {
                is LocalCacheSettings.Persistent -> persistentLocalCache(
                    json(
                        "cacheSizeBytes" to cacheSettings.sizeBytes,
                    ).asDynamic() as PersistentCacheSettings,
                )
                is LocalCacheSettings.Memory -> {
                    val garbageCollecorSettings = when (val garbageCollectorSettings = cacheSettings.garbaseCollectorSettings) {
                        is MemoryGarbageCollectorSettings.Eager -> memoryEagerGarbageCollector()
                        is MemoryGarbageCollectorSettings.LRUGC -> memoryLruGarbageCollector(json("cacheSizeBytes" to garbageCollectorSettings.sizeBytes))
                    }
                    memoryLocalCache(json("garbageCollector" to garbageCollecorSettings).asDynamic() as MemoryCacheSettings)
                }
            },
        )
    }
}

actual fun firestoreSettings(
    settings: FirebaseFirestoreSettings?,
    builder: FirebaseFirestoreSettings.Builder.() -> Unit,
): FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder().apply {
    settings?.let {
        sslEnabled = it.sslEnabled
        host = it.host
        cacheSettings = it.cacheSettings
    }
}.apply(builder).build()

actual data class NativeWriteBatch(val js: JsWriteBatch)

val WriteBatch.js get() = native.js

actual data class NativeTransaction(val js: JsTransaction)

val Transaction.js get() = native.js

/** A class representing a platform specific Firebase DocumentReference. */
actual typealias NativeDocumentReferenceType = JsDocumentReference

val DocumentReference.js get() = native.js

actual open class NativeQuery(open val js: JsQuery)
internal val JsQuery.wrapped get() = NativeQuery(this)

val Query.js get() = native.js

actual data class NativeCollectionReference(override val js: JsCollectionReference) : NativeQuery(js)

val CollectionReference.js get() = native.js

actual class FirebaseFirestoreException(cause: Throwable, val code: FirestoreExceptionCode) : FirebaseException(code.toString(), cause)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual class QuerySnapshot(val js: JsQuerySnapshot) {
    actual val documents
        get() = js.docs.map { DocumentSnapshot(NativeDocumentSnapshotWrapper(it)) }
    actual val documentChanges
        get() = js.docChanges().map { DocumentChange(it) }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(js.metadata)
}

actual class DocumentChange(val js: JsDocumentChange) {
    actual val document: DocumentSnapshot
        get() = DocumentSnapshot(NativeDocumentSnapshotWrapper(js.doc))
    actual val newIndex: Int
        get() = js.newIndex
    actual val oldIndex: Int
        get() = js.oldIndex
    actual val type: ChangeType
        get() = ChangeType.values().first { it.jsString == js.type }
}

actual data class NativeDocumentSnapshot(val js: JsDocumentSnapshot)

val DocumentSnapshot.js get() = native.js

actual class SnapshotMetadata(val js: JsSnapshotMetadata) {
    actual val hasPendingWrites: Boolean get() = js.hasPendingWrites
    actual val isFromCache: Boolean get() = js.fromCache
}

actual class FieldPath private constructor(val js: JsFieldPath) {

    actual companion object {
        actual val documentId = FieldPath(jsDocumentId())
    }
    actual constructor(vararg fieldNames: String) : this(
        dev.gitlive.firebase.firestore.rethrow {
            JsFieldPath(*fieldNames)
        },
    )
    actual val documentId: FieldPath get() = FieldPath.documentId
    actual val encoded: EncodedFieldPath = js
    override fun equals(other: Any?): Boolean = other is FieldPath && js.isEqual(other.js)
    override fun hashCode(): Int = js.hashCode()
    override fun toString(): String = js.toString()
}

actual typealias EncodedFieldPath = JsFieldPath

actual enum class FirestoreExceptionCode {
    OK,
    CANCELLED,
    UNKNOWN,
    INVALID_ARGUMENT,
    DEADLINE_EXCEEDED,
    NOT_FOUND,
    ALREADY_EXISTS,
    PERMISSION_DENIED,
    RESOURCE_EXHAUSTED,
    FAILED_PRECONDITION,
    ABORTED,
    OUT_OF_RANGE,
    UNIMPLEMENTED,
    INTERNAL,
    UNAVAILABLE,
    DATA_LOSS,
    UNAUTHENTICATED,
}

actual enum class Direction(internal val jsString: String) {
    ASCENDING("asc"),
    DESCENDING("desc"),
}

actual enum class ChangeType(internal val jsString: String) {
    ADDED("added"),
    MODIFIED("modified"),
    REMOVED("removed"),
}

inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.firestore.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch (e: dynamic) {
        throw errorToException(e)
    }
}

fun errorToException(e: dynamic) = (e?.code ?: e?.message ?: "")
    .toString()
    .lowercase()
    .let {
        when {
            "cancelled" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.CANCELLED)
            "invalid-argument" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.INVALID_ARGUMENT)
            "deadline-exceeded" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.DEADLINE_EXCEEDED)
            "not-found" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.NOT_FOUND)
            "already-exists" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.ALREADY_EXISTS)
            "permission-denied" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.PERMISSION_DENIED)
            "resource-exhausted" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.RESOURCE_EXHAUSTED)
            "failed-precondition" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.FAILED_PRECONDITION)
            "aborted" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.ABORTED)
            "out-of-range" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.OUT_OF_RANGE)
            "unimplemented" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNIMPLEMENTED)
            "internal" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.INTERNAL)
            "unavailable" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNAVAILABLE)
            "data-loss" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.DATA_LOSS)
            "unauthenticated" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNAUTHENTICATED)
            "unknown" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNKNOWN)
            else -> {
                println("Unknown error code in ${JSON.stringify(e)}")
                FirebaseFirestoreException(e, FirestoreExceptionCode.UNKNOWN)
            }
        }
    }

// from: https://discuss.kotlinlang.org/t/how-to-access-native-js-object-as-a-map-string-any/509/8
fun entriesOf(jsObject: dynamic): List<Pair<String, Any?>> =
    (js("Object.entries") as (dynamic) -> Array<Array<Any?>>)
        .invoke(jsObject)
        .map { entry -> entry[0] as String to entry[1] }

// from: https://discuss.kotlinlang.org/t/how-to-access-native-js-object-as-a-map-string-any/509/8
fun mapOf(jsObject: dynamic): Map<String, Any?> =
    entriesOf(jsObject).toMap()
