/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore.internal

import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.filterAnd
import com.google.firebase.firestore.filterArrayContains
import com.google.firebase.firestore.filterArrayContainsAny
import com.google.firebase.firestore.filterEqualTo
import com.google.firebase.firestore.filterGreaterThan
import com.google.firebase.firestore.filterGreaterThanOrEqualTo
import com.google.firebase.firestore.filterInArray
import com.google.firebase.firestore.filterLessThan
import com.google.firebase.firestore.filterLessThanOrEqualTo
import com.google.firebase.firestore.filterNotEqualTo
import com.google.firebase.firestore.filterNotInArray
import com.google.firebase.firestore.filterOr
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.memoryEagerGcSettings
import com.google.firebase.firestore.memoryLruGcSettings
import com.google.firebase.firestore.persistentCacheSettings
import dev.gitlive.firebase.firestore.Filter
import dev.gitlive.firebase.firestore.LocalCacheSettings
import dev.gitlive.firebase.firestore.MemoryGarbageCollectorSettings
import dev.gitlive.firebase.firestore.WhereConstraint
import dev.gitlive.firebase.internal.EncodedObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.google.firebase.firestore.DocumentReference as CompatDocumentReference
import com.google.firebase.firestore.DocumentSnapshot as CompatDocumentSnapshot
import com.google.firebase.firestore.FieldPath as CompatFieldPath
import com.google.firebase.firestore.Filter as CompatFilter
import com.google.firebase.firestore.FirebaseFirestore as CompatFirebaseFirestore
import com.google.firebase.firestore.LocalCacheSettings as CompatLocalCacheSettings
import com.google.firebase.firestore.Query as CompatQuery
import com.google.firebase.firestore.QuerySnapshot as CompatQuerySnapshot
import com.google.firebase.firestore.Transaction as CompatTransaction

/*
 * The seams between the dev.gitlive layer and the com.google.firebase.firestore layer that differ per platform: how
 * encoded data is handed to the SDK, how document data is read back for the decoders (a plain JS value on JS), the
 * platform's asynchronous transaction reads, and the executor or dispatch queue the listeners are called on.
 */

/** The encoded document as the map the compatibility layer writes. */
internal expect fun EncodedObject.toCompatMap(): Map<String, Any?>

/** The data as the platform SDK reads it (a plain JS value on JS), which the decoders take. */
internal expect fun CompatDocumentSnapshot.nativeData(serverTimestampBehavior: CompatDocumentSnapshot.ServerTimestampBehavior): Any?

/** The value of [field] as the platform SDK reads it (a plain JS value on JS), which the decoders take. */
internal expect fun CompatDocumentSnapshot.nativeGet(field: String, serverTimestampBehavior: CompatDocumentSnapshot.ServerTimestampBehavior): Any?

/** The value of [fieldPath] as the platform SDK reads it (a plain JS value on JS), which the decoders take. */
internal expect fun CompatDocumentSnapshot.nativeGet(fieldPath: CompatFieldPath, serverTimestampBehavior: CompatDocumentSnapshot.ServerTimestampBehavior): Any?

/** Runs [updateFunction] in a transaction; the JS SDK takes a promise-returning function, the others block the transaction's thread. */
internal expect suspend fun <T> CompatFirebaseFirestore.runSuspendTransaction(updateFunction: suspend (CompatTransaction) -> T): T

/** Reads [documentRef] inside the transaction, asynchronously on JS. */
internal expect suspend fun CompatTransaction.getAwait(documentRef: CompatDocumentReference): CompatDocumentSnapshot

/** Listens to the query on the executor configured through the settings on Android, the SDK's default elsewhere. */
internal expect fun CompatQuery.wrapperSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<CompatQuerySnapshot>): ListenerRegistration

/** Listens to the document on the executor configured through the settings on Android, the SDK's default elsewhere. */
internal expect fun CompatDocumentReference.wrapperSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<CompatDocumentSnapshot>): ListenerRegistration

internal fun CompatQuery.wrapperSnapshots(includeMetadataChanges: Boolean): Flow<CompatQuerySnapshot> = callbackFlow {
    val registration = wrapperSnapshotListener(if (includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE) { snapshot, error ->
        if (error != null) {
            close(error)
        } else if (snapshot != null) {
            trySend(snapshot)
        }
    }
    awaitClose { registration.remove() }
}

internal fun CompatDocumentReference.wrapperSnapshots(includeMetadataChanges: Boolean): Flow<CompatDocumentSnapshot> = callbackFlow {
    val registration = wrapperSnapshotListener(if (includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE) { snapshot, error ->
        if (error != null) {
            close(error)
        } else if (snapshot != null) {
            trySend(snapshot)
        }
    }
    awaitClose { registration.remove() }
}

/** The filter as the compatibility layer's [CompatFilter]. */
internal fun Filter.toCompat(): CompatFilter = when (this) {
    is Filter.And -> filterAnd(filters.map { it.toCompat() })
    is Filter.Or -> filterOr(filters.map { it.toCompat() })
    is Filter.Field -> when (val constraint = constraint) {
        is WhereConstraint.EqualTo -> filterEqualTo(field, constraint.value)
        is WhereConstraint.NotEqualTo -> filterNotEqualTo(field, constraint.value)
        is WhereConstraint.LessThan -> filterLessThan(field, constraint.value)
        is WhereConstraint.GreaterThan -> filterGreaterThan(field, constraint.value)
        is WhereConstraint.LessThanOrEqualTo -> filterLessThanOrEqualTo(field, constraint.value)
        is WhereConstraint.GreaterThanOrEqualTo -> filterGreaterThanOrEqualTo(field, constraint.value)
        is WhereConstraint.ArrayContains -> filterArrayContains(field, constraint.value)
        is WhereConstraint.ArrayContainsAny -> filterArrayContainsAny(field, constraint.values)
        is WhereConstraint.InArray -> filterInArray(field, constraint.values)
        is WhereConstraint.NotInArray -> filterNotInArray(field, constraint.values)
    }
    is Filter.Path -> when (val constraint = constraint) {
        is WhereConstraint.EqualTo -> filterEqualTo(path.compat, constraint.value)
        is WhereConstraint.NotEqualTo -> filterNotEqualTo(path.compat, constraint.value)
        is WhereConstraint.LessThan -> filterLessThan(path.compat, constraint.value)
        is WhereConstraint.GreaterThan -> filterGreaterThan(path.compat, constraint.value)
        is WhereConstraint.LessThanOrEqualTo -> filterLessThanOrEqualTo(path.compat, constraint.value)
        is WhereConstraint.GreaterThanOrEqualTo -> filterGreaterThanOrEqualTo(path.compat, constraint.value)
        is WhereConstraint.ArrayContains -> filterArrayContains(path.compat, constraint.value)
        is WhereConstraint.ArrayContainsAny -> filterArrayContainsAny(path.compat, constraint.values)
        is WhereConstraint.InArray -> filterInArray(path.compat, constraint.values)
        is WhereConstraint.NotInArray -> filterNotInArray(path.compat, constraint.values)
    }
}

/** The cache settings as the compatibility layer's, built through the Kotlin extensions and fluent setters (real static methods on Android). */
internal fun LocalCacheSettings.toCompat(): CompatLocalCacheSettings = when (this) {
    is LocalCacheSettings.Persistent -> persistentCacheSettings { setSizeBytes(sizeBytes) }
    is LocalCacheSettings.Memory -> memoryCacheSettings {
        setGcSettings(
            when (val gc = garbaseCollectorSettings) {
                is MemoryGarbageCollectorSettings.Eager -> memoryEagerGcSettings { }
                is MemoryGarbageCollectorSettings.LRUGC -> memoryLruGcSettings { setSizeBytes(gc.sizeBytes) }
            },
        )
    }
}
